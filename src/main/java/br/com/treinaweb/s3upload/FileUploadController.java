package br.com.treinaweb.s3upload;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.annotation.PostConstruct;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.model.UploadResult;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	@Value("${cloud.aws.region.static}")
	private String region;

	private AmazonS3 s3Client;

	private TransferManager transferManager;

	@PostConstruct
	public void init() {
		s3Client = AmazonS3ClientBuilder.standard().withRegion(region).build();
		transferManager = TransferManagerBuilder.standard().withS3Client(s3Client).build();
	}

	@PostMapping("/upload")
	public String uploadFile(@RequestPart(value = "file") MultipartFile file) {
		try {
			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentType(file.getContentType());

			UploadResult result = transferManager.upload(
				bucket, "uploads/" + file.getOriginalFilename(), 
				file.getInputStream(), objectMetadata
			).waitForUploadResult();

			return result.getKey();
		} catch (Exception e) {
			e.printStackTrace();
			return "error ";
		}
	}

	@GetMapping("/file/{key}")
	public String getFile(@PathVariable("key") String key) {
		Date inTwoHours = Date.from(Instant.now().plus(2, ChronoUnit.HOURS));

		return s3Client.generatePresignedUrl(bucket, key, inTwoHours).toString();
	}
}
