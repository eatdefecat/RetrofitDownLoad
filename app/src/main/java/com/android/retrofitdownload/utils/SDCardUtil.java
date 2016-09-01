package com.android.retrofitdownload.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class SDCardUtil {
	
	/**
	 * 获取可用存储空间的根目录，一般先获取外置存储空间，如果没有，再去获取内存存储空间，
	 * 如果都没有，则获取本地应用的可用目录
	 * @param context
	 * @return
	 */
	public static String getRootDir(Context context) {
		//先找外置存储路径
		String state = Environment.getExternalStorageState();
		if(Environment.MEDIA_MOUNTED.equals(state)) {
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		}
			
		//再找内置SDCard
		for(VoldFstab vold : mVolds) {
			File mount = new File(vold.mMountPoint);
			if(mount.exists()
					&& mount.canRead()
					&& mount.canWrite()
					&& mount.canExecute()) {
				return mount.getAbsolutePath();
			}
		}
				
		//再找本地应用内存路径
		if(context != null) {
			return context.getFilesDir().getAbsolutePath();
		} else {
			Log.e("", "Context is null");
		}
		
		return null;
	}

	/**
	 * 获取指定目录剩余存储空间，返回单位为字节
	 * @param directory
	 * @return
	 */
	public static long getLeftSpace(String directory) {
		if(TextUtils.isEmpty(directory)) return 0;
		
		long space = 0;
		try {
			StatFs sf = new StatFs(directory);
			space = (long)sf.getBlockSize() * sf.getAvailableBlocks();
		} catch (Exception ex) {
			return 0;
		} 
		
		return space;
	}
	
	/**
	 * 获取指定目录所有存储空间, 返回单位为字节
	 * @param directory
	 * @return
	 */
	public static long getTotalSpace(String directory) {
		if(TextUtils.isEmpty(directory)) return 0;
		
		long space = 0;
		try {
			StatFs sf = new StatFs(directory);
			space = (long)sf.getBlockSize() * sf.getBlockCount();
		} catch (Exception ex) {
			return 0;
		} 
		
		return space;
	}
	
	
	private static final String DEV_MOUNT = "dev_mount";
	private static ArrayList<VoldFstab> mVolds;

	public static boolean isRoot() {
		String binPath  = "/system/bin/su";
		String xBinPath = "/system/xbin/su";
		return new File(binPath).exists() && isExecutable(binPath) || new File(xBinPath).exists() && isExecutable(xBinPath);
	}

	private static boolean isExecutable(String filePath) {
		Process p = null;
		try {
			p = Runtime.getRuntime().exec("ls -l " + filePath);
			// 获取返回内容
			BufferedReader in = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String str = in.readLine();
			if (str != null && str.length() >= 4) {
				char flag = str.charAt(3);
				if (flag == 's' || flag == 'x')
					return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (p != null) {
				p.destroy();
			}
		}
		return false;
	}

	static {
		mVolds = new ArrayList<VoldFstab>();

		if(isRoot()){
			Log.i("tag", "==========is root");
			BufferedReader reader = null;
			try {
				//vold.fstab文件
				File file = new File(Environment.getRootDirectory().getAbsoluteFile()
						+ File.separator
						+ "etc"
						+ File.separator
						+ "vold.fstab");
				reader = new BufferedReader(new FileReader(file));
				String line = null;

				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (line.startsWith(DEV_MOUNT)) {
						String[] infos = line.split(" ");
						VoldFstab vold = new VoldFstab();
						vold.mLabel = infos[1];  //设置标签
						vold.mMountPoint = infos[2].split(":")[0];//设置挂载点
						vold.mPart = infos[3];//设置子分区个数
						vold.mSysfs = infos[4].split(":");//设置设备在sysfs文件系统下的路径
						mVolds.add(vold);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				try {
					if(reader != null) reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}else{
			Log.i("tag" , "==========no root");
		}
	}

	private static class VoldFstab {
		//标签
		public String mLabel;
		//挂载点
		public String mMountPoint;
		//子分区个数
		public String mPart;
		//设备在sysfs文件系统下的路径
		public String[] mSysfs;
	}


	public static String getAppCacheDir(Context context) {

		// 获取外置sd卡
		File file = context.getExternalCacheDir();
		if(file != null){
			String sdCacheDirectory = file.getPath();
			long sdSpace = SDCardUtil.getLeftSpace(sdCacheDirectory) / 1024 / 1024;
			if(sdSpace > 30){
				return sdCacheDirectory;
			}
		}

		//内置SDCard
		for(VoldFstab vold : mVolds) {
			File mount = new File(vold.mMountPoint);
			if(mount.exists() && mount.canRead() && mount.canWrite() && mount.canExecute()) {
				String sdCache2Directory = mount.getAbsolutePath();
				long sd2Space = SDCardUtil.getLeftSpace(sdCache2Directory) / 1024 / 1024;
				if(sd2Space > 30){
					sdCache2Directory = sdCache2Directory + File.separator + "retrofit";
					return sdCache2Directory;
				}
			}
		}

		// 获取系统存储
		String cacheDirectory = context.getCacheDir().getPath();
		long cacheSpace = SDCardUtil.getLeftSpace(cacheDirectory) / 1024 / 1024;
		if (cacheSpace > 30) {
			return cacheDirectory;
		}else{
			Toast.makeText(context, "手机存储空间不足!", Toast.LENGTH_SHORT).show();
			return null;
		}
	}

	public static String getImageCacheDir(Context context) {
		String rootDir = getAppCacheDir(context);
		if(TextUtils.isEmpty(rootDir)) return null;

		String path = rootDir + File.separator + "image";
		FileUtil.makeFolders(path);
		return path;
	}

	public static String getLogCacheDir(Context context) {
		String rootDir = getAppCacheDir(context);
		if(TextUtils.isEmpty(rootDir)) return null;

		String path = rootDir + File.separator + "log";
		FileUtil.makeFolders(path);
		return path;
	}
}
