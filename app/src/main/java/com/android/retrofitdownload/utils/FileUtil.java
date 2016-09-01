package com.android.retrofitdownload.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtil {

	public static final String ASSETS_PREFIX = "file://android_assets/";
	public static final String ASSETS_PREFIX2 = "file://android_asset/";
	public static final String ASSETS_PREFIX3 = "assets://";
	public static final String ASSETS_PREFIX4 = "asset://";
	public static final String RAW_PREFIX = "file://android_raw/";
	public static final String RAW_PREFIX2 = "raw://";
	public static final String FILE_PREFIX = "file://";
	public static final String DRAWABLE_PREFIX = "drawable://";
	public static final String TAG = "FileUtil";

	/**
	 * 缓冲区大小
	 */
	private static final int BUFFER_SIZE = 100 * 1024;

	/**
	 * uri转图片路径<BR>
	 * 
	 * @param activity
	 *            Activity
	 * @param uri
	 *            uri
	 * @return 图片路径
	 */
	public static String getImagePath(Activity activity, Uri uri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		String imgpath = null;
		Cursor imagecursor = activity.managedQuery(uri, proj, null, null, null);
		try {

			if (null != imagecursor && imagecursor.moveToFirst()) {
				int imagecolumnindex = imagecursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				imgpath = imagecursor.getString(imagecolumnindex);
			}
		} catch (Exception e) {
			Log.d(TAG, "an error occured while running getImagePath : " + e);
		}
		return imgpath;
	}

	/**
	 * 复制文件
	 * 
	 * @param origin
	 *            原始文件
	 * @param dest
	 *            目标文件
	 * @return 是否复制成功
	 */
	public static boolean copyFile(File origin, File dest) {
		if (origin == null || dest == null) {
			return false;
		}
		if (!dest.exists()) {
			File parentFile = dest.getParentFile();
			if (!parentFile.exists()) {
				boolean succeed = parentFile.mkdirs();
				if (!succeed) {
					Log.i(TAG, "copyFile failed, cause mkdirs return false");
					return false;
				}
			}
			try {
				dest.createNewFile();
			} catch (Exception e) {
				Log.i(TAG, "copyFile failed, cause createNewFile failed");
				return false;
			}
		}
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(origin);
			out = new FileOutputStream(dest);
			FileChannel inC = in.getChannel();
			FileChannel outC = out.getChannel();
			int length = BUFFER_SIZE;
			while (true) {
				if (inC.position() == inC.size()) {
					return true;
				}
				if ((inC.size() - inC.position()) < BUFFER_SIZE) {
					length = (int) (inC.size() - inC.position());
				} else {
					length = BUFFER_SIZE;
				}
				inC.transferTo(inC.position(), length, outC);
				inC.position(inC.position() + length);
			}
		} catch (Exception e) {
			return false;
		} finally {
			closeStream(in);
			closeStream(out);
		}
	}

	/**
	 * 专门用来关闭可关闭的流
	 * 
	 * @param beCloseStream
	 *            需要关闭的流
	 * @return 已经为空或者关闭成功返回true，否则返回false
	 */
	public static boolean closeStream(java.io.Closeable beCloseStream) {
		if (beCloseStream != null) {
			try {
				beCloseStream.close();
				return true;
			} catch (IOException e) {
				Log.e(TAG, "close stream error", e);
				return false;
			}
		}
		return true;
	}

	
    /**
     * 根据文件夹路径，创建目录
     * @param fileDir
     * @return
     */
    public static boolean makeFolders(String fileDir) {
    	if (TextUtils.isEmpty(fileDir)) {
            return false;
        }

        File folder = new File(fileDir);
        return (folder.exists() && folder.isDirectory()) ? true : folder.mkdirs();
    }
    
    
    /**
	 * 创建文件
	 * @param filePath
	 * @return
	 */
	public static boolean createFile(String filePath) {
		if(TextUtils.isEmpty(filePath)) return false;
		
		File file = new File(filePath);
		if(file.exists()) return true;
		
		boolean result = false;
		try {
			if(makeDirs(filePath)) result = file.createNewFile();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * 根据文件路径，创建目录
	 * @param filePath
	 * @return
	 */
	public static boolean makeDirs(String filePath) {
        String folderName = getFolderName(filePath);
        if (TextUtils.isEmpty(folderName)) {
            return false;
        }

        File folder = new File(folderName);
        return (folder.exists() && folder.isDirectory()) ? true : folder.mkdirs();
    }
	
	public static String getFolderName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }

        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? "" : filePath.substring(0, filePosi);
    }

	// 获取文件的大小
    public static long getFileSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return -1;
        }

        File file = new File(path);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }

	/**
	 * 文件是否存在
	 * @return
	 */
    public static boolean isFileExist(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        File file = new File(filePath);
        return (file.exists() && file.isFile());
    }


	/**
	 * sdcard是否可用
	 * @return true为可用，否则为不可用
	 */
	public static boolean sdCardIsAvailable() {
		String status = Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED))
			return false;
		return true;
	}

	public static InputStream getStream(Context context, String url) throws IOException {
		String lowerUrl = url.toLowerCase();
		InputStream is;
		if (lowerUrl.startsWith(ASSETS_PREFIX)) {
			String assetPath = url.substring(ASSETS_PREFIX.length());
			is = getAssetsStream(context, assetPath);
		} else if (lowerUrl.startsWith(ASSETS_PREFIX2)) {
			String assetPath = url.substring(ASSETS_PREFIX2.length());
			is = getAssetsStream(context, assetPath);
		} else if (lowerUrl.startsWith(ASSETS_PREFIX3)) {
			String assetPath = url.substring(ASSETS_PREFIX3.length());
			is = getAssetsStream(context, assetPath);
		} else if (lowerUrl.startsWith(ASSETS_PREFIX4)) {
			String assetPath = url.substring(ASSETS_PREFIX4.length());
			is = getAssetsStream(context, assetPath);
		} else if (lowerUrl.startsWith(RAW_PREFIX)) {
			String rawName = url.substring(RAW_PREFIX.length());
			is = getRawStream(context, rawName);
		} else if (lowerUrl.startsWith(RAW_PREFIX2)) {
			String rawName = url.substring(RAW_PREFIX2.length());
			is = getRawStream(context, rawName);
		} else if (lowerUrl.startsWith(FILE_PREFIX)) {
			String filePath = url.substring(FILE_PREFIX.length());
			is = getFileStream(filePath);
		} else if (lowerUrl.startsWith(DRAWABLE_PREFIX)) {
			String drawableName = url.substring(DRAWABLE_PREFIX.length());
			is = getDrawableStream(context, drawableName);
		} else {
			throw new IllegalArgumentException(String.format("Unsupported url: %s \n" +
					"Supported: \n%sxxx\n%sxxx\n%sxxx", url, ASSETS_PREFIX, RAW_PREFIX, FILE_PREFIX));
		}
		return is;
	}

	private static InputStream getAssetsStream(Context context, String path) throws IOException {
		return context.getAssets().open(path);
	}

	private static InputStream getFileStream(String path) throws IOException {
		return new FileInputStream(path);
	}

	private static InputStream getRawStream(Context context, String rawName) throws IOException {
		int id = context.getResources().getIdentifier(rawName, "raw", context.getPackageName());
		if (id != 0) {
			try {
				return context.getResources().openRawResource(id);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		throw new IOException(String.format("raw of id: %s from %s not found", id, rawName));
	}

	private static InputStream getDrawableStream(Context context, String rawName) throws IOException {
		int id = context.getResources().getIdentifier(rawName, "drawable", context.getPackageName());
		if (id != 0) {
			BitmapDrawable drawable = (BitmapDrawable) context.getResources().getDrawable(id);
			Bitmap bitmap = drawable.getBitmap();

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
			return new ByteArrayInputStream(os.toByteArray());
		}

		throw new IOException(String.format("bitmap of id: %s from %s not found", id, rawName));
	}

	public static String getString(Context context, String url) throws IOException {
		return getString(context, url, "UTF-8");
	}

	public static String getString(Context context, String url, String encoding) throws IOException {
		String result = readStreamString(getStream(context, url), encoding);
		if (result.startsWith("\ufeff")) {
			result = result.substring(1);
		}

		return result;
	}

	public static String readStreamString(InputStream is, String encoding) throws IOException {
		return new String(readStream(is), encoding);
	}

	public static byte[] readStream(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024 * 10];
		int readlen;
		while ((readlen = is.read(buf)) >= 0) {
			baos.write(buf, 0, readlen);
		}
		baos.close();

		return baos.toByteArray();
	}

	/**
	 * 将raw文件拷贝到指定目录
	 */
	public static void copyRawFile(Context ctx, String rawFileName, String to) {
		String[] names = rawFileName.split("\\.");
		String toFile = to + "/" + names[0] + "." + names[1];
		File file = new File(toFile);
		if (file.exists()) {
			return;
		}
		try {
			InputStream is = getStream(ctx, "raw://" + names[0]);
			OutputStream os = new FileOutputStream(toFile);
			int byteCount = 0;
			byte[] bytes = new byte[1024];

			while ((byteCount = is.read(bytes)) != -1) {
				os.write(bytes, 0, byteCount);
			}
			os.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解压缩功能.
	 * 将ZIP_FILENAME文件解压到ZIP_DIR目录下.
	 *
	 * @param zipFile    压缩文件
	 * @param folderPath 解压目录
	 */
	public static int unZipFile(File zipFile, String folderPath) {
		ZipFile zfile = null;
		try {
			zfile = new ZipFile(zipFile);
			Enumeration zList = zfile.entries();
			ZipEntry ze = null;
			byte[] buf = new byte[1024];
			while (zList.hasMoreElements()) {
				ze = (ZipEntry) zList.nextElement();
				if (ze.isDirectory()) {
					String dirstr = folderPath + ze.getName();
					//dirstr.trim();
					dirstr = new String(dirstr.getBytes("8859_1"), "GB2312");
					File f = new File(dirstr);
					f.mkdir();
					continue;
				}
				OutputStream os = new BufferedOutputStream(new FileOutputStream(getRealFileName(folderPath, ze.getName())));
				InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
				int readLen = 0;
				while ((readLen = is.read(buf)) != -1) {
					os.write(buf, 0, readLen);
				}
				is.close();
				os.close();
			}
			zfile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0;
	}

	/**
	 * 给定根目录，返回一个相对路径所对应的实际文件名.
	 *
	 * @param baseDir     指定根目录
	 * @param absFileName 相对路径名，来自于ZipEntry中的name
	 * @return java.io.File 实际的文件
	 */
	private static File getRealFileName(String baseDir, String absFileName) {
		String[] dirs = absFileName.split("/");
		File ret = new File(baseDir);
		String substr = null;
		if (dirs.length > 1) {
			for (int i = 0; i < dirs.length - 1; i++) {
				substr = dirs[i];
				try {
					substr = new String(substr.getBytes("8859_1"), "GB2312");

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				ret = new File(ret, substr);

			}
			if (!ret.exists())
				ret.mkdirs();
			substr = dirs[dirs.length - 1];
			try {
				substr = new String(substr.getBytes("8859_1"), "GB2312");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			ret = new File(ret, substr);
			return ret;
		}
		return ret;
	}

	/**
	 * 通过流创建文件
	 */
	public static void createFileFormInputStream(InputStream is, String path) {
		try {
			FileOutputStream fos = new FileOutputStream(path);
			byte[] buf = new byte[1376];
			while (is.read(buf) > 0) {
				fos.write(buf, 0, buf.length);
			}
			is.close();
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 拷贝文件
	 *
	 * @param fromPath
	 * @param toPath
	 * @return
	 */
	public static boolean copy(String fromPath, String toPath) {
		File file = new File(fromPath);
		if (!file.exists()) {
			return false;
		}
		createFile(toPath);
		return copyFile(fromPath, toPath);
	}

	/**
	 * 拷贝文件
	 *
	 * @param fromFile
	 * @param toFile
	 * @return
	 */
	private static boolean copyFile(String fromFile, String toFile) {
		InputStream fosfrom = null;
		OutputStream fosto = null;
		try {
			fosfrom = new FileInputStream(fromFile);
			fosto = new FileOutputStream(toFile);
			byte bt[] = new byte[1024];
			int c;
			while ((c = fosfrom.read(bt)) > 0) {
				fosto.write(bt, 0, c);
			}
			return true;

		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		} finally {
			try {
				if (fosfrom != null) {
					fosfrom.close();
				}
				if (fosto != null) {
					fosto.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * 创建文件 当文件不存在的时候就创建一个文件，否则直接返回文件
	 *
	 * @param path
	 * @return
	 */
	public static File createFiles(String path) {
		File file = new File(path);
		if (!file.getParentFile().exists()) {
			Log.d(TAG, "目标文件所在路径不存在，准备创建……");
			if (!createDir(file.getParent())) {
				Log.d(TAG, "创建目录文件所在的目录失败！文件路径【" + path + "】");
			}
		}
		// 创建目标文件
		try {
			if (!file.exists()) {
				if (file.createNewFile()) {
					Log.d(TAG, "创建文件成功:" + file.getAbsolutePath());
				}
				return file;
			} else {
				return file;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 创建目录 当目录不存在的时候创建文件，否则返回false
	 *
	 * @param path
	 * @return
	 */
	public static boolean createDir(String path) {
		File file = new File(path);
		if (!file.exists()) {
			if (!file.mkdirs()) {
				Log.d(TAG, "创建失败，请检查路径和是否配置文件权限！");
				return false;
			}
			return true;
		}
		return false;
	}


	/**
	 * 拷贝Assets中的文件到指定目录
	 *
	 * @param context
	 * @param fileName
	 * @param path
	 * @return
	 */
	public static boolean copyFileFromAssets(Context context, String fileName, String path) {
		boolean copyIsFinish = false;
		try {
			InputStream is   = context.getAssets().open(fileName);
			File file = FileUtil.createFiles(path);
			FileOutputStream fos  = new FileOutputStream(file);
			byte[]           temp = new byte[1024];
			int              i    = 0;
			while ((i = is.read(temp)) > 0) {
				fos.write(temp, 0, i);
			}
			fos.close();
			is.close();
			copyIsFinish = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return copyIsFinish;
	}

}
