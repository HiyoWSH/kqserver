package kq.server.util;


import java.io.*;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import org.apache.log4j.Logger;


/**
 * @author wangsh
 * @e-mail wanghiyo@163.com
 * @date 2016年12月6日 上午10:58:39
 * @描述：文件操作类
 * @注意事项：
 */
// 我的文件操作库
public class FileOperate {

	private static final Logger logger = Logger.getLogger(FileOperate.class);
	/**
	 *
	 * @param fileLocation 文件读取 路径
	 * @param coding 文件读取 编码
	 * @return List<String>
	 */
	public static List<String> readFileToStringList(String fileLocation, String coding) {
		List<String> list = new LinkedList<String>();
		try {
			BufferedReader br = getBufferedReaderFromFile(fileLocation, coding);
			String strRead = null;
			while (br.ready()) {
				strRead = br.readLine();
				list.add(strRead);
			}
			br.close();
		} catch (Exception e) {
			logger.error("读取文件" + fileLocation + "失败");
			e.printStackTrace();
		}
		return list;
	}

	public static StringBuilder readFileToStringBuilder(String fileLocation, String coding) {
		StringBuilder bulider = new StringBuilder();
		try {
			BufferedReader br = getBufferedReaderFromFile(fileLocation, coding);
			String strRead = null;
			while (br.ready()) {
				strRead = br.readLine();
				bulider.append(strRead);
				bulider.append("\n");
			}
			br.close();
		} catch (Exception e) {
			logger.error("读取文件" + fileLocation + "失败" + e.getMessage());
			e.printStackTrace();
		}
		return bulider;
	}

	public static JSONObject readJsonFile(String path, String coding) {
		String jsonTexct = readFileToStringBuilder(path, coding).toString();
		return JSONObject.parseObject(jsonTexct);
	}

	public static BufferedReader getBufferedReaderFromFile(String fileLocation, String coding) throws FileNotFoundException, UnsupportedEncodingException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(fileLocation), coding));
	}

	public static BufferedWriter getBufferedWriterToFile(String fileLocation, String coding) throws FileNotFoundException, UnsupportedEncodingException {
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileLocation), coding));
	}

	/**
	 *
	 * @param strList  写入内容，没文件会自动创建
	 * @param fileLocation 路径
	 * @param coding 编码
	 */
	public static void writeFile(List<String> strList, String fileLocation, String coding) {
		try {
			BufferedWriter bw = getBufferedWriterToFile(fileLocation, coding);
			for (String s : strList) {
				bw.write(s);
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			System.out.println("写入文件" + fileLocation + "失败，错误IO");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("写入文件" + fileLocation + "失败，未知错误");
			e.printStackTrace();
		}
	}

	/**
	 * 文件是否存在
	 * @param s
	 * @return
	 */
	public static boolean fileExists(String s){
		File f = new File(s);
		return f.exists();
	}

	public static boolean createFile(String s){
		File f =new File(s);
		if(!f.exists()) {
			try {
				f.createNewFile();
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	/**
	 * 创建目录
	 * @param dirName 目录
	 */
	public static void createDir(String dirName){
		File f=new File(dirName);
		f.mkdirs();//创建一个文件夹与他所有父文件夹
	}

	public static void writeFileFromString(String string, String fileLocation ,String coding) {
		try {
			BufferedWriter bw = getBufferedWriterToFile(fileLocation, coding);
			bw.write(string);
			bw.newLine();
			bw.flush();
			bw.close();
		} catch (IOException e) {
			System.out.println("写入文件" + fileLocation + "失败，错误IO");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("写入文件" + fileLocation + "失败，未知错误");
			e.printStackTrace();
		}
	}

}
