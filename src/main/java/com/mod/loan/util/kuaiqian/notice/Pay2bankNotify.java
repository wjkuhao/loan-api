package com.mod.loan.util.kuaiqian.notice;

import javax.xml.bind.annotation.*;

/**
 * 
 * @ClassName: Notify
 * @Description: 通知对象
 * @date 2017-3-30
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)  
@XmlRootElement  
@XmlType(name = "pay2bankNotify", propOrder = {"membercode","merchant_id","apply_date","order_seq_id","fee",
		"status","error_code","error_msg","amt","bank","name","bank_card_no","end_date"})  
public class Pay2bankNotify {
	
	@XmlElement(required = true) 
	private String membercode="";
	
	@XmlElement(required = true) 
	private String merchant_id="";

	@XmlElement(required = true) 
	private String apply_date="";
	
	@XmlElement(required = true) 
	private String order_seq_id="";
	
	@XmlElement(required = true) 
	private String fee="";
	
	@XmlElement(required = true) 
	private String status="";
	
	@XmlElement(required = true) 
	private String error_code="";
	
	@XmlElement(required = true) 
	private String error_msg="";
	
	@XmlElement(required = true) 
	private String amt="";
	
	@XmlElement(required = true) 
	private String bank="";

	@XmlElement(required = true) 
	private String name="";
	
	@XmlElement(required = true) 
	private String bank_card_no="";
	
	@XmlElement(required = true) 
	private String end_date="";

	public String getMembercode() {
		return membercode;
	}

	public void setMembercode(String membercode) {
		this.membercode = membercode;
	}

	public String getMerchant_id() {
		return merchant_id;
	}

	public void setMerchant_id(String merchant_id) {
		this.merchant_id = merchant_id;
	}

	public String getApply_date() {
		return apply_date;
	}

	public void setApply_date(String apply_date) {
		this.apply_date = apply_date;
	}

	public String getOrder_seq_id() {
		return order_seq_id;
	}

	public void setOrder_seq_id(String order_seq_id) {
		this.order_seq_id = order_seq_id;
	}

	public String getFee() {
		return fee;
	}

	public void setFee(String fee) {
		this.fee = fee;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getError_code() {
		return error_code;
	}

	public void setError_code(String error_code) {
		this.error_code = error_code;
	}

	public String getError_msg() {
		return error_msg;
	}

	public void setError_msg(String error_msg) {
		this.error_msg = error_msg;
	}

	public String getAmt() {
		return amt;
	}

	public void setAmt(String amt) {
		this.amt = amt;
	}

	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBank_card_no() {
		return bank_card_no;
	}

	public void setBank_card_no(String bank_card_no) {
		this.bank_card_no = bank_card_no;
	}

	public String getEnd_date() {
		return end_date;
	}

	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}


	


	
}
