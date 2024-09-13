package com.etyc.constant;

public interface Constants {

	String AUTHORIZATION_HEADER = "Authorization";
	String JWT_USER_KEY = "userId";

	long ACCESS_TOKEN_TTL = 60 * 60 * 24L;
	long REFRESH_TOKEN_TTL = 60 * 60 * 24 * 7L;
	long LARGEST_TIMESTAMP = 2147483647;
	long WEEK_TIMESTAMP_SPAN = 604800000;

	String PHONE_REGEX = "^(?:\\+\\d{1,3}\\s?)?(\\(?\\d{1,4}\\)?[\\s-]?)?\\d{7,15}$\n";
	String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$\n";

	String OSS_FILE_BUCKET = "etyc-file";

}
