package com.di.toolkit.img;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import com.di.toolkit.FileUtil;
import com.di.toolkit.img.BufferedImageUtil;
import com.di.toolkit.img.Pixel;

/**
 * @author di
 */
public class ImageRecognition {

	public static String parseBest(String dataPath, String targetPath) {
		return parseBest(dataPath, BufferedImageUtil.read(targetPath));
	}

	public static String parseBest(String dataPath, BufferedImage img) {
		String content = FileUtil.readAsString(dataPath, "GBK");
		TrainData td = TrainDataSerilization.deSerilize(content);
		HashMap<String, Double> map = new HashMap<>();
		Pixel background = BufferedImageUtil.getPixel(img, 0, 0);
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				Pixel p = BufferedImageUtil.getPixel(img, x, y);
				if (!BufferedImageUtil.isSimilar(p, background, ImageTrain.range)) {
					for (Char c : td.getCs()) {
						Pixel p0 = c.getPixels().get(0);
						if (BufferedImageUtil.isSimilar(p, p0, ImageTrain.range)) {
							int count = 1;
							for (int i = 1; i < c.getPixels().size(); i++) {
								Pixel pi = c.getPixels().get(i);
								int x_ = pi.getX() - p0.getX();
								int y_ = pi.getY() - p0.getY();
								if ((x + x_) < img.getWidth() && (y + y_) < img.getHeight()) {
									Pixel p_ = BufferedImageUtil.getPixel(img, x + x_, y + y_);
									if (p_ != null && !BufferedImageUtil.isSimilar(background, p_, ImageTrain.range)) {
										count++;
									}
								}
								double d = (double) count / (double) c.getKeyPoint();
								if (map.get(c.getC()) == null || (map.get(c.getC()) != null && map.get(c.getC()) < d)) {
									map.put(c.getC(), d);
									// System.out.println(c.getC() + " : " +
									// count + " / " + c.getKeyPoint() + " = "
									// + (double) count / (double)
									// c.getKeyPoint());
								}
							}
						}
					}
				}
			}
		}
		double max = 0;
		String key = " ";
		for (String s : map.keySet()) {
			if (map.get(s) > max) {
				max = map.get(s);
				key = s;
			}
		}
		if (key.indexOf("L") != -1 && map.get("U") != null && map.get("U") > 0.9) {
			if (map.get("D") != null && map.get("D") > 0.9) {
				return "D";
			} else if (map.get("U") != null && map.get("U") > 0.9) {
				return "U";
			}
		} else {
			System.out.println("best match is " + key + " ratio is " + max * 100 + "%");
		}
		return key.substring(0, 1);
	}
}
