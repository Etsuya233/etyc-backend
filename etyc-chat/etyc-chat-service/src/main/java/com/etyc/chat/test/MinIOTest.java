package com.etyc.chat.test;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MinIOTest {
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
		try {
			MinioClient minioClient =
					MinioClient.builder()
							.endpoint("http://192.168.150.101:9000")
							.credentials("2wOEOLfASQmrufY2scS1", "qRif4ywxlkgedQ6dKUSqVx9ktXdhdYqtEf6xUbOE")
							.build();

			// Make 'etyc-avatar' bucket if not exist.
			boolean found =
					minioClient.bucketExists(BucketExistsArgs.builder().bucket("etyc-avatar").build());
			if (!found) {
				// Make a new bucket called 'etyc-avatar'.
				minioClient.makeBucket(MakeBucketArgs.builder().bucket("etyc-avatar").build());
			} else {
				System.out.println("Bucket 'etyc-avatar' already exists.");
			}
		} catch (MinioException e) {
			System.out.println("Error occurred: " + e);
			System.out.println("HTTP trace: " + e.httpTrace());
		}
	}
}
