package de.loosensimnetz.iot.raspi.motor;

public class ExpectedTime {
	private final long expectedTime, tolerance;

	public ExpectedTime(long expectedTime, long tolerance) {
		super();
		this.expectedTime = expectedTime;
		this.tolerance = tolerance;
	}

	public long getExpectedTime() {
		return expectedTime;
	}

	public long getTolerance() {
		return tolerance;
	}

	@Override
	public String toString() {
		return "ExpectedTime [expectedTime=" + expectedTime + ", tolerance=" + tolerance + "]";
	}
}
