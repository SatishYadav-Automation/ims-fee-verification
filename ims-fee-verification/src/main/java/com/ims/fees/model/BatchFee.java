package com.ims.fees.model;

public record BatchFee(String code, String name, int baseFee, int gstPercent, int gstAmount, String startDate,
		String batchType) {

	/** One readable line, used in failure messages. */
	@Override
	public String toString() {
		return name + " | code " + (code.isEmpty() ? "none" : code) + " | base fee " + baseFee + " | GST " + gstPercent
				+ "% = " + gstAmount + " | start " + startDate + " | type "
				+ (batchType.isEmpty() ? "none" : batchType);
	}
}