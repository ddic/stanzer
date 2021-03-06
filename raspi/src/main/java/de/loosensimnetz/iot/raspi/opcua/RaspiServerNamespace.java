/*
 * Copyright (c) 2016 Kevin Herron
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.html.
 */

package de.loosensimnetz.iot.raspi.opcua;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.AccessContext;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.MethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.api.Namespace;
import org.eclipse.milo.opcua.sdk.server.api.nodes.VariableNode;
import org.eclipse.milo.opcua.sdk.server.nodes.AttributeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.ServerNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.nodes.delegates.AttributeDelegate;
import org.eclipse.milo.opcua.sdk.server.nodes.delegates.AttributeDelegateChain;
import org.eclipse.milo.opcua.sdk.server.util.AnnotationBasedInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue;
import org.eclipse.milo.opcua.stack.core.util.FutureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import de.loosensimnetz.iot.raspi.motor.Motor;
import de.loosensimnetz.iot.raspi.motor.MotorSensor;
import de.loosensimnetz.iot.raspi.opcua.ExpectedTimeMethod.TimeType;

public class RaspiServerNamespace implements Namespace {

	public static final String NAMESPACE_URI = "urn:de.loosensimnetz:iot:raspiserver";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Manages subscriptions to OPC UA events
	 */
	private final SubscriptionModel subscriptionModel;

	private final OpcUaServer server;
	private final UShort namespaceIndex;

	/**
	 * The motor model for this OPC UA server
	 */
	private final Motor motor;
	
	/**
	 * The motor sensor
	 */
	private final MotorSensor motorSensor;

	public RaspiServerNamespace(OpcUaServer server, UShort namespaceIndex, Motor motor, MotorSensor motorSensor) {
		this.server = server;
		this.namespaceIndex = namespaceIndex;
		this.motor = motor;
		this.motorSensor = motorSensor;

		subscriptionModel = new SubscriptionModel(server, this);

		createRaspiServerFolder();
	}

	/**
	 * Helper method to create the folder for the RaspberryPi server
	 */
	private void createRaspiServerFolder() {
		try {
			// Create a "RaspiServer" folder and add it to the node manager
			NodeId folderNodeId = new NodeId(namespaceIndex, "RaspiServer");

			UaFolderNode folderNode = new UaFolderNode(server.getNodeMap(), folderNodeId,
					new QualifiedName(namespaceIndex, "RaspiServer"), LocalizedText.english("RaspiServer"));

			server.getNodeMap().addNode(folderNode);

			// Make sure our new folder shows up under the server's Objects folder
			server.getUaNamespace().addReference(Identifiers.ObjectsFolder, Identifiers.Organizes, true,
					folderNodeId.expanded(), NodeClass.Object);

			// Add the motor nodes
			addMotorNodes(folderNode);

			// Create a "Leds" folder and add it to the "RaspiServer" folder
			NodeId ledsFolderNodeId = new NodeId(namespaceIndex, "RaspiServer/Leds");

			UaFolderNode ledFolderNode = new UaFolderNode(server.getNodeMap(), ledsFolderNodeId,
					new QualifiedName(namespaceIndex, "RaspiServer/Leds"), LocalizedText.english("Leds"));

			server.getNodeMap().addNode(ledFolderNode);
			folderNode.addOrganizes(ledFolderNode);

			// Add the led nodes
			addLedNodes(ledFolderNode, 1);
			addLedNodes(ledFolderNode, 2);
		} catch (UaException e) {
			logger.error("Error adding nodes: {}", e.getMessage(), e);
		}
	}

	@Override
	public UShort getNamespaceIndex() {
		return namespaceIndex;
	}

	@Override
	public String getNamespaceUri() {
		return NAMESPACE_URI;
	}

	/**
	 * Add the dynamic nodes to the root node
	 * 
	 * @param rootNode
	 *            Root folder
	 */
	private void addMotorNodes(UaFolderNode rootNode) {
		UaFolderNode motorFolder = new UaFolderNode(server.getNodeMap(),
				new NodeId(namespaceIndex, "RaspiServer/Motor"), new QualifiedName(namespaceIndex, "Motor"),
				LocalizedText.english("Motor"));

		server.getNodeMap().addNode(motorFolder);
		rootNode.addOrganizes(motorFolder);

		addMotorVariables(motorFolder);
		addMotorMethods(motorFolder);
	}

	/**
	 * 
	 * @param folder
	 */
	private void addMotorMethods(UaFolderNode folderNode) {
		// Method setExpectedTimeDown
		String methodName = "setExpectedTimeDown(expectedTime, tolerance)";
		String methodId = folderNode.getNodeId().getIdentifier() + "/" + methodName; // "RaspiServer/Motor" + "/" +
																						// "setExpectedTimeDown"
		addMethod(methodName, methodId, folderNode, new ExpectedTimeMethod(motor, TimeType.TIME_DOWN),
				"Set the expected time and tolerance in milliseconds for the motor to move from the initial position downward to the lower end position. Returns the values before the invocation.");

		// Method setExpectedTimeStoppedDown
		methodName = "setExpectedTimeStoppedDown(expectedTime, tolerance)";
		methodId = folderNode.getNodeId().getIdentifier() + "/" + methodName; // "RaspiServer/Motor" + "/" +
																				// "setExpectedTimeStoppedDown"
		addMethod(methodName, methodId, folderNode, new ExpectedTimeMethod(motor, TimeType.TIME_STOPPED_DOWN),
				"Set the expected time and tolerance in milliseconds for the motor in lower end position. Returns the values before the invocation.");

		// Method setExpectedTimeUp
		methodName = "setExpectedTimeUp(expectedTime, tolerance)";
		methodId = folderNode.getNodeId().getIdentifier() + "/" + methodName; // "RaspiServer/Motor" + "/" +
																				// "setExpectedTimeUp"
		addMethod(methodName, methodId, folderNode, new ExpectedTimeMethod(motor, TimeType.TIME_UP),
				"Set the expected time and tolerance in milliseconds for the motor to move from the lower end position upward to the initial position. Returns the values before the invocation.");
		
		// Method reset
		methodName = "reset()";
		methodId = folderNode.getNodeId().getIdentifier() + "/" + methodName; // "RaspiServer/Motor" + "/" +
																				// "reset"
		addMethod(methodName, methodId, folderNode, new MotorResetStateMethod(motorSensor),
				"Manually reset the state of the motor to STOPPED_INITIAL.");
	}

	/**
	 * Add the variables for the motor folder
	 * 
	 * @param motorFolder
	 *            Motor folder ("RaspiServer/Motor")
	 */
	private void addMotorVariables(UaFolderNode motorFolder) {
		// Dynamic Boolean MotorDown
		addDynamicVariable(motorFolder, Identifiers.Boolean, "MotorDown",
				AttributeDelegateChain.create(new AttributeDelegate() {
					@Override
					public DataValue getValue(AttributeContext context, VariableNode node) throws UaException {
						return new DataValue(new Variant(motor.isMovingDown()));
					}
				}, ValueLoggingDelegate::new));

		// Dynamic Boolean MotorUp
		addDynamicVariable(motorFolder, Identifiers.Boolean, "MotorUp",
				AttributeDelegateChain.create(new AttributeDelegate() {
					@Override
					public DataValue getValue(AttributeContext context, VariableNode node) throws UaException {
						return new DataValue(new Variant(motor.isMovingUp()));
					}
				}, ValueLoggingDelegate::new));

		// Dynamic Boolean MotorMoving
		addDynamicVariable(motorFolder, Identifiers.Boolean, "MotorMoving",
				AttributeDelegateChain.create(new AttributeDelegate() {
					@Override
					public DataValue getValue(AttributeContext context, VariableNode node) throws UaException {
						return new DataValue(new Variant(motor.isMovingUp() || motor.isMovingDown()));
					}
				}, ValueLoggingDelegate::new));
		
		// Dynamic String MotorState
		addDynamicVariable(motorFolder, Identifiers.String, "MotorState",
				AttributeDelegateChain.create(new AttributeDelegate() {
					@Override
					public DataValue getValue(AttributeContext context, VariableNode node) throws UaException {
						return new DataValue(new Variant(motorSensor.getStateId()));
					}
				}, ValueLoggingDelegate::new));
	}

	/**
	 * Add the nodes for the two leds to the folder in our namespace
	 * 
	 * @param folderNode
	 *            Root folder for the leds ("RaspiServer/Leds")
	 * @param ledNumber
	 *            Number of the led to add (1 or 2)
	 */
	private void addLedNodes(UaFolderNode folderNode, int ledNumber) {
		String ledName = "Led" + ledNumber;
		String folderId = folderNode.getNodeId().getIdentifier() + "/" + ledName; // "RaspiServer/Leds/Led<#>"
		String methodName = "turnOn(x)";
		String methodId = folderId + "/" + methodName;

		UaFolderNode ledFolder = new UaFolderNode(server.getNodeMap(), new NodeId(namespaceIndex, folderId),
				new QualifiedName(namespaceIndex, folderId), LocalizedText.english("Led number " + ledNumber));

		folderNode.addOrganizes(ledFolder);

		// Method node for method turnLed<#>On(x) - <#> either 1 or 2
		addMethod(methodName, methodId, ledFolder, new LedStateMethod(motor, ledNumber),
				"Turns the led on (x = true) or off (x = false). Returns the state of the led before the invocation.");

		// Dynamic Boolean LedOn
		addLedVariable(ledNumber, ledFolder);
	}

	/**
	 * Add method
	 * 
	 * @param methodName
	 *            Method name
	 * @param methodId
	 *            ID of the method
	 * @param methodFolder
	 *            Led folder
	 */
	private void addMethod(String methodName, String methodId, UaFolderNode methodFolder, OpcMethod method,
			String description) {
		UaMethodNode methodNode = UaMethodNode.builder(server.getNodeMap())
				.setNodeId(new NodeId(namespaceIndex, methodId))
				.setBrowseName(new QualifiedName(namespaceIndex, methodName))
				.setDisplayName(new LocalizedText(null, methodName)).setDescription(LocalizedText.english(description))
				.build();

		try {
			AnnotationBasedInvocationHandler invocationHandler = AnnotationBasedInvocationHandler
					.fromAnnotatedObject(server.getNodeMap(), method);

			methodNode.setProperty(UaMethodNode.InputArguments, invocationHandler.getInputArguments());
			methodNode.setProperty(UaMethodNode.OutputArguments, invocationHandler.getOutputArguments());
			methodNode.setInvocationHandler(invocationHandler);

			server.getNodeMap().addNode(methodNode);
			methodFolder.addOrganizes(methodNode);

			methodFolder.addReference(new Reference(methodFolder.getNodeId(), Identifiers.HasComponent,
					methodNode.getNodeId().expanded(), methodNode.getNodeClass(), true));

			methodNode.addReference(new Reference(methodNode.getNodeId(), Identifiers.HasComponent,
					methodFolder.getNodeId().expanded(), methodFolder.getNodeClass(), false));
		} catch (Exception e) {
			logger.error("Error creating " + methodName + "() method.", e);
		}
	}

	/**
	 * Add led variable LenOn for led <#>
	 * 
	 * @param ledNumber
	 *            Led number
	 * @param ledFolder
	 *            Led folder
	 */
	private void addLedVariable(int ledNumber, UaFolderNode ledFolder) {
		LedStateBoolean ledStateBoolean = new LedStateBoolean(motor, ledNumber);

		addDynamicVariable(ledFolder, Identifiers.Boolean, "LedOn",
				AttributeDelegateChain.create(new AttributeDelegate() {
					@Override
					public DataValue getValue(AttributeContext context, VariableNode node) throws UaException {
						return new DataValue(new Variant(ledStateBoolean.getState()));
					}
				}, ValueLoggingDelegate::new));
	}

	/**
	 * Helper method to add a dynamic read-only attribute node to
	 * <code>dynamicFolder</code>
	 * 
	 * @param dynamicFolder
	 *            Folder
	 * @param nodeName
	 *            Name of the node
	 * @param attributeDelegate
	 *            Delegate for the node value
	 */
	private void addDynamicVariable(UaFolderNode dynamicFolder, NodeId typeId, String nodeName,
			AttributeDelegate attributeDelegate) {
		String name = nodeName;
		Variant variant = new Variant(false);

		UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(server.getNodeMap())
				.setNodeId(new NodeId(namespaceIndex, dynamicFolder.getNodeId().getIdentifier() + "/" + name))
				.setAccessLevel(ubyte(AccessLevel.getMask(AccessLevel.READ_ONLY)))
				.setBrowseName(new QualifiedName(namespaceIndex, name)).setDisplayName(LocalizedText.english(name))
				.setDataType(typeId).setTypeDefinition(Identifiers.BaseDataVariableType).build();

		node.setValue(new DataValue(variant));

		node.setAttributeDelegate(attributeDelegate);

		server.getNodeMap().addNode(node);
		dynamicFolder.addOrganizes(node);
	}

	@Override
	public CompletableFuture<List<Reference>> browse(AccessContext context, NodeId nodeId) {
		ServerNode node = server.getNodeMap().get(nodeId);

		if (node != null) {
			return CompletableFuture.completedFuture(node.getReferences());
		} else {
			return FutureUtils.failedFuture(new UaException(StatusCodes.Bad_NodeIdUnknown));
		}
	}

	@Override
	public void read(ReadContext context, Double maxAge, TimestampsToReturn timestamps,
			List<ReadValueId> readValueIds) {

		List<DataValue> results = Lists.newArrayListWithCapacity(readValueIds.size());

		for (ReadValueId readValueId : readValueIds) {
			ServerNode node = server.getNodeMap().get(readValueId.getNodeId());

			if (node != null) {
				DataValue value = node.readAttribute(new AttributeContext(context), readValueId.getAttributeId(),
						timestamps, readValueId.getIndexRange());

				results.add(value);
			} else {
				results.add(new DataValue(StatusCodes.Bad_NodeIdUnknown));
			}
		}

		context.complete(results);
	}

	@Override
	public void write(WriteContext context, List<WriteValue> writeValues) {
		List<StatusCode> results = Lists.newArrayListWithCapacity(writeValues.size());

		for (WriteValue writeValue : writeValues) {
			ServerNode node = server.getNodeMap().get(writeValue.getNodeId());

			if (node != null) {
				try {
					node.writeAttribute(new AttributeContext(context), writeValue.getAttributeId(),
							writeValue.getValue(), writeValue.getIndexRange());

					results.add(StatusCode.GOOD);

					logger.info("Wrote value {} to {} attribute of {}", writeValue.getValue().getValue(),
							AttributeId.from(writeValue.getAttributeId()).map(Object::toString).orElse("unknown"),
							node.getNodeId());
				} catch (UaException e) {
					logger.error("Unable to write value={}", writeValue.getValue(), e);
					results.add(e.getStatusCode());
				}
			} else {
				results.add(new StatusCode(StatusCodes.Bad_NodeIdUnknown));
			}
		}

		context.complete(results);
	}

	@Override
	public void onDataItemsCreated(List<DataItem> dataItems) {
		subscriptionModel.onDataItemsCreated(dataItems);
	}

	@Override
	public void onDataItemsModified(List<DataItem> dataItems) {
		subscriptionModel.onDataItemsModified(dataItems);
	}

	@Override
	public void onDataItemsDeleted(List<DataItem> dataItems) {
		subscriptionModel.onDataItemsDeleted(dataItems);
	}

	@Override
	public void onMonitoringModeChanged(List<MonitoredItem> monitoredItems) {
		subscriptionModel.onMonitoringModeChanged(monitoredItems);
	}

	@Override
	public Optional<MethodInvocationHandler> getInvocationHandler(NodeId methodId) {
		Optional<ServerNode> node = server.getNodeMap().getNode(methodId);

		return node.flatMap(n -> {
			if (n instanceof UaMethodNode) {
				return ((UaMethodNode) n).getInvocationHandler();
			} else {
				return Optional.empty();
			}
		});
	}

}
