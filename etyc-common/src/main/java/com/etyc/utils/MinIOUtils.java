package com.etyc.utils;

import com.etyc.constant.Constants;
import com.etyc.exceptions.EtycException;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@ConditionalOnClass(MinioClient.class)
@RequiredArgsConstructor
@Slf4j
public class MinIOUtils {

	private final MinioClient minioClient;

	public void putObject(String bucket, String filename, String contentType, InputStream stream, long size){
		try {
			if(!minioClient.bucketExists(BucketExistsArgs.builder().bucket(Constants.OSS_FILE_BUCKET).build())){
				log.info("桶 {} 不存在，现已创建！", Constants.OSS_FILE_BUCKET);
				minioClient.makeBucket(MakeBucketArgs.builder().bucket(Constants.OSS_FILE_BUCKET).build());
			}

			PutObjectArgs putObjectArgs = PutObjectArgs.builder()
					.object(filename)
					.stream(stream, size, -1)
					.contentType(contentType)
					.bucket(bucket)
					.build();

			minioClient.putObject(putObjectArgs);
		} catch (Exception e) {
			throw new EtycException("文件上传出现异常!", e);
		}
	}

}
