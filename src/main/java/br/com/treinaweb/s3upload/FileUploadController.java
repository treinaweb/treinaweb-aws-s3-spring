package br.com.treinaweb.s3upload;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.model.UploadResult;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/storage/")
public class FileUploadController {

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	@PostMapping("/upload")
	public String uploadFile(@RequestPart(value = "file") final MultipartFile file) {
		try {
			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentType(file.getContentType());

			TransferManager transferManager = TransferManagerBuilder.defaultTransferManager();
			UploadResult result = transferManager.upload(
				bucket, "uploads/" + file.getName(), 
				file.getInputStream(), objectMetadata
			).waitForUploadResult();

			return result.getKey();
		} catch (Exception e) {
			return "error " + e.getStackTrace();
		}
	}
}
