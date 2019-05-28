package com.mod.loan.util.aliyun;

import com.aliyun.oss.OSSClient;
import com.mod.loan.config.Constant;
import com.mod.loan.controller.UploadController;
import com.mod.loan.util.Base64ToMultipartFileUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public class OSSUtil {

	private static Logger logger = LoggerFactory.getLogger(UploadController.class);

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

	private static String uploadBase64(MultipartFile file, String fileType) {
		OSSClient ossClient = new OSSClient(endPointUrl(Constant.ENVIROMENT), Constant.OSS_ACCESSKEY_ID,
				Constant.OSS_ACCESS_KEY_SECRET);
		String filepath = "";
		try {
			DateTime dateTime = new DateTime();
			String newFileName = UUID.randomUUID().toString().replaceAll("-", "") + fileType;
			filepath = dateTime.getYear() + "/" + dateTime.toString("MMdd") + "/" + newFileName;
			ossClient.putObject(Constant.OSS_STATIC_BUCKET_NAME, filepath, file.getInputStream());
		} catch (Exception e) {
			logger.error("文件上传失败 ", e);
		} finally {
			ossClient.shutdown();
		}
		return filepath;
	}

    public static String uploadAuthImage(String base64, String fileType){
        MultipartFile livingPhotoFile = Base64ToMultipartFileUtil.base64ToMultipart(base64);
        return OSSUtil.uploadBase64(livingPhotoFile, fileType);
    }

	/**
	 * 根据环境切换上传地址
	 * 
	 * @param env
	 * @return
	 */
	private static String endPointUrl(String env) {
		if ("dev".equals(env)) {
			return Constant.OSS_ENDPOINT_OUT_URL;
		}
		return Constant.OSS_ENDPOINT_IN_URL;
	}

}
