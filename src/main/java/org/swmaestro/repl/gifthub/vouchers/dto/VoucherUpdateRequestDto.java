package org.swmaestro.repl.gifthub.vouchers.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class VoucherUpdateRequestDto {
	private String barcode;
	private String expiresAt;
	private String productName;
	private String brandName;

	@Builder
	public VoucherUpdateRequestDto(String barcode, String expiresAt, String productName, String brandName) {
		this.barcode = barcode;
		this.expiresAt = expiresAt;
		this.productName = productName;
		this.brandName = brandName;
	}
}
