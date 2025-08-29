package com.bajaj.challenge;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class ChallengeApp implements CommandLineRunner {

	RestTemplate rt = new RestTemplate();
	String name = "Sakshi Ahuja";
	String regNo = "22BCE5070";
	String email = "sakshi.ahuja2022@vitstudent.ac.in";

	String genUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
	String submitUrlDefault = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

	public static void main(String[] args) {
		SpringApplication.run(ChallengeApp.class, args);
	}

	@Override
	public void run(String... args) {
		try {
			//send my details to get webhook + token
			Map<String, String> input = new HashMap<>();
			input.put("name", name);
			input.put("regNo", regNo);
			input.put("email", email);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			Map res1 = rt.postForObject(genUrl, new HttpEntity<>(input, headers), Map.class);
			String webhook = res1.get("webhook").toString();
			String token = res1.get("accessToken").toString();

			System.out.println("Webhook: " + webhook);
			System.out.println("Token: " + token);

			String finalQuery =
					"SELECT p.AMOUNT AS SALARY, " +
							"CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
							"YEAR(CURDATE()) - YEAR(e.DOB) AS AGE, " +
							"d.DEPARTMENT_NAME " +
							"FROM PAYMENTS p " +
							"JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
							"JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
							"WHERE DAY(p.PAYMENT_TIME) != 1 " +
							"ORDER BY p.AMOUNT DESC " +
							"LIMIT 1;";

			HttpHeaders subHeaders = new HttpHeaders();
			subHeaders.setContentType(MediaType.APPLICATION_JSON);
			subHeaders.setBearerAuth(token);

			Map<String, Object> submitBody = new HashMap<>();
			submitBody.put("finalQuery", finalQuery);

			String submitUrl = (webhook != null && !webhook.isBlank()) ? webhook : submitUrlDefault;

			ResponseEntity<String> res2;
			try {
				res2 = rt.postForEntity(submitUrl, new HttpEntity<>(submitBody, subHeaders), String.class);
			} catch (Exception e) {
				//retry with raw token
				HttpHeaders altHeaders = new HttpHeaders();
				altHeaders.setContentType(MediaType.APPLICATION_JSON);
				altHeaders.add("Authorization", token);

				res2 = rt.postForEntity(submitUrl, new HttpEntity<>(submitBody, altHeaders), String.class);
			}

			System.out.println("Status: " + res2.getStatusCode());
			System.out.println("Response: " + res2.getBody());

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
}
