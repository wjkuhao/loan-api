package com.mod.loan.util.aliyun;

import com.aliyun.oss.OSSClient;
import com.mod.loan.config.Constant;
import com.mod.loan.controller.UploadController;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public class OSSUtil {

	private static Logger logger = LoggerFactory.getLogger(UploadController.class);

	private static String endpoint_out = "https://oss-cn-hangzhou.aliyuncs.com";// 外网
	private static String endpoint_in = "https://oss-cn-hangzhou-internal.aliyuncs.com";// 内网

	public static String upload(MultipartFile file) {
		String fileName = file.getOriginalFilename();
		String fileType = fileName.substring(fileName.lastIndexOf("."));
		OSSClient ossClient = new OSSClient(endPointUrl(Constant.ENVIROMENT), Constant.OSS_ACCESSKEY_ID,
				Constant.OSS_ACCESS_KEY_SECRET);
		String filepath = "";
		try {
			DateTime dateTime = new DateTime();
			String newFileName = UUID.randomUUID().toString().replaceAll("-", "") + fileType;
			filepath = dateTime.getYear() + "/" + dateTime.toString("MMdd") + "/" + newFileName;
			ossClient.putObject(Constant.OSS_STATIC_BUCKET_NAME, filepath, file.getInputStream());
		} catch (Exception e) {
			logger.error("文件上传失败", e);
		} finally {
			if (ossClient != null) {
				ossClient.shutdown();
			}
		}
		return filepath;
	}

	/**
	 * 根据环境切换上传地址
	 * 
	 * @param env
	 * @return
	 */
	public static String endPointUrl(String env) {
		if ("dev".equals(env)) {
			return OSSUtil.endpoint_out;
		}
		return OSSUtil.endpoint_in;
	}

}
