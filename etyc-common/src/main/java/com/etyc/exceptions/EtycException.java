package com.etyc.exceptions;

import com.etyc.enums.ExceptionEnum;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
public class EtycException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;

	private int code = 1001;
	private String msg = "error!";
	private int status = 500;

	public EtycException(String msg) {
		super(msg);
		this.msg = msg;
	}

	public EtycException(String msg, Throwable e) {
		super(msg, e);
		this.msg = msg;
	}

	public EtycException(String msg, int code) {
		super(msg);
		this.msg = msg;
		this.code = code;
	}

	public EtycException(String msg, int code, Throwable e) {
		super(msg, e);
		this.msg = msg;
		this.code = code;
	}

	public EtycException(ExceptionEnum exceptionEnum){
		super(exceptionEnum.msg);
		this.msg = exceptionEnum.msg;
		this.code = exceptionEnum.code;
	}

	public EtycException(ExceptionEnum exceptionEnum, String msg){
		super(msg);
		this.msg = msg;
		this.code = exceptionEnum.code;
	}

	public EtycException(ExceptionEnum exceptionEnum, Throwable e){
		super(exceptionEnum.msg, e);
		this.msg = exceptionEnum.msg;
		this.code = exceptionEnum.code;
	}
}