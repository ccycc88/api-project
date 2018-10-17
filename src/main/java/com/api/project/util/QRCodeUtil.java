package com.api.project.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

public class QRCodeUtil {

	public static void createQrcode(String _text, File qrcodeFile,
									int qrcodeWidth, int qrcodeHeight,
									String qrcodeFormat) throws IOException, WriterException {
	
		try {

			HashMap<EncodeHintType, String> hints = new HashMap<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
			BitMatrix bitMatrix = new MultiFormatWriter().encode(_text, BarcodeFormat.QR_CODE, qrcodeWidth,
					qrcodeHeight, hints);

			BufferedImage image = new BufferedImage(qrcodeWidth, qrcodeHeight, BufferedImage.TYPE_INT_RGB);
			ImageIO.write(image, qrcodeFormat, qrcodeFile);
			
			MatrixToImageWriter.writeToStream(bitMatrix, qrcodeFormat, new FileOutputStream(qrcodeFile));
		} catch (Exception e) {
			throw e;
		}
	}

	public static String decodeQr(String filePath) throws IOException, NotFoundException {
		String retStr = "";
		if (StringUtil.isBlank(filePath)) {
			throw new IllegalArgumentException("无效的图片路径");
		}
		try {
			BufferedImage bufferedImage = ImageIO.read(new FileInputStream(filePath));
			LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
			Binarizer binarizer = new HybridBinarizer(source);
			BinaryBitmap bitmap = new BinaryBitmap(binarizer);
			HashMap<DecodeHintType, Object> hintTypeObjectHashMap = new HashMap<DecodeHintType, Object>();
			hintTypeObjectHashMap.put(DecodeHintType.CHARACTER_SET, "UTF-8");
			Result result = new MultiFormatReader().decode(bitmap, hintTypeObjectHashMap);
			retStr = result.getText();
		} catch (Exception e) {
			throw e;
		}
		return retStr;
	}
}