package com.docusign.report.cipher;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.docusign.report.common.constant.AppConstants;
import com.docusign.report.common.exception.BatchAlgorithmException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AESCipher {

	private static byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private static IvParameterSpec ivspec = new IvParameterSpec(iv);

	public static SecretKey setKey(String secKey, String salt) {

		try {

			SecretKeyFactory factory = SecretKeyFactory.getInstance(AppConstants.SECRETKEY_TYPE);
			KeySpec spec = new PBEKeySpec(secKey.trim().toCharArray(), salt.trim().getBytes(), 65536, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

			return secretKey;
		} catch (NoSuchAlgorithmException e) {

			throw new BatchAlgorithmException("Error in creating Key, algo issue");
		} catch (InvalidKeySpecException e) {

			e.printStackTrace();
			throw new BatchAlgorithmException("Error in creating Key, invalid key");
		}

	}

	public static String encrypt(String strToEncrypt, String secKey, String salt) {

		try {

			Cipher cipher = Cipher.getInstance(AppConstants.CIPHER_INSTANCE_TYPE);
			cipher.init(Cipher.ENCRYPT_MODE, setKey(secKey, salt), ivspec);
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.trim().getBytes("UTF-8")));

		} catch (Exception e) {

			log.error("Error while encrypting: {} with message {}", e.toString(), e.getMessage());
			throw new BatchAlgorithmException("Error in encryption " + e.getMessage());
		}
	}

	public static String decrypt(String strToDecrypt, String secKey, String salt) {

		try {

			Cipher cipher = Cipher.getInstance(AppConstants.CIPHER_INSTANCE_TYPE);
			cipher.init(Cipher.DECRYPT_MODE, setKey(secKey, salt), ivspec);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));

		} catch (Exception e) {

			log.error("Error while decrypting: {} with message {}", e.toString(), e.getMessage());
			throw new BatchAlgorithmException("Error in decryption " + e.getMessage());
		}
	}

}