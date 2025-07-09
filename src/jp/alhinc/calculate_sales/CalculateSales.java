package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	//プログラムはmainメソッドから動く
	public static void main(String[] args) {
		
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理を呼び出す、その時に引数を４つ持ってっている
		//引数…順番(型)、個数　これがあっていないと、エラーになる
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return; //　falseならここで終わる　trueなら店定義ファイル読み込み処に進む
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)

		//指定したパスに存在する全てのファイル(または、ディレクトリ)の情報を格納します +　files…売上ファイルもあれば、branch.lstも混ざっている
		File[] files = new File (args[0]).listFiles();

		//rcdFiles…売上ファイルのみが格納されるリスト
		List<File> rcdFiles = new ArrayList<>();

		//全てのファイルを取得　要素数の
		for(int i = 0; i < files.length ; i++) {
		
			//ファイル名取得
			String fileName = files[i].getName();
	
			//matches を使⽤してファイル名が「数字8桁.rcd」なのか判定します。
			if(fileName.matches("[0-9]{8}.{rcd}")) {
		    
				//trueの場合の処理
				rcdFiles.add(files[i]);
		
			}

		}
		//以上までが該当のファイルを保持にすぎない
		
		//ここからファイルの取り出し	→ 	読み込み[String型]	→	取り出し	→	型変換
		
		//取り出し	rcdFilesに複数の売上ファイルの情報を格納しているので、その数だけ繰り返します。
		for(int i = 0; i < rcdFiles.size(); i++) {
		
		//読み込み
			BufferedReader br = null;

			try {
				File file = new File("rcdFiles");
				//
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				String line;
				// 一行ずつ読み込む	+	nullじゃない限り繰り返す
				while((line = br.readLine()) != null) {
					
					//型の変換 売上ファイルから読み込んだ売上金額をMapに加算していくために、
					long fileSale = Long.parseLong(line); 
					System.out.println(fileSale);
				
				
						}

			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				
				return false;
			} finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return false;
					}
				}
			}
			return true;			
			
			
		
		
		
		

		}

		
		
		
		
		
		

		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理を下記にて行う
	 *必要な引数が上から4つ
	 * @param フォルダパス　aegs[0]が渡ってきていて、その中身は"C:\Users\trainee1277\Desktop\売り上げ集計課題"
	 * @param ファイル名     branch.lst
	 * @param 支店コードと支店名を保持するMap（ branch.lst）
	 * @param 支店コードと売上金額を保持するMap(00001 rec～)
	 * @return 読み込み可否
	 */
	//mainメソッドから呼び出される
	//引数4つ
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			//
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
			String[] items = line.split(",");
				//reallinedで1行づつ読み込んだ結果をlineに代入している　
				//nullじゃない限り繰り返す
				// ※ここの読み込み処理を変更してください。(処理内容1-2)

			branchNames.put(items[0],items[1]);
		    branchSales.put(items[0], 0L);

				System.out.println(line);
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)

		return true;
	}

}
