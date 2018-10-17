package com.api.project.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializeUtil {

	public static void writeObjToFile(Serializable obj, String file) {
		try {
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
			os.writeObject(obj);
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Serializable readObjFromFile(String file) {
		ObjectInputStream is = null;
		Serializable obj = null;
		try {
			is = new ObjectInputStream(new FileInputStream(file));
			obj = (Serializable) is.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		}
		return obj;

	}
}
