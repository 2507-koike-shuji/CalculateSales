package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

		if (args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();

		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理を呼び出す、その時に引数を４つ持ってっている
		//引数…順番(型)、個数　これがあっていないと、エラーになる
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return; //　falseならここで終わる　trueなら店定義ファイル読み込み処に進む
		}
		//指定したパスに存在する全てのファイル(または、ディレクトリ)の情報を格納します +files…売上ファイルもあれば、branch.lstも混ざっている
		File[] files = new File(args[0]).listFiles();

		//rcdFiles…売上ファイルのみが格納されるリスト
		List<File> rcdFiles = new ArrayList<>();

		//全てのファイルを取得　要素数の数を繰り返す
		for (int i = 0; i < files.length; i++) {

			//ファイル名取得
			String fileName = files[i].getName();

			//matches を使⽤してファイル名が「数字8桁.rcd」なのか判定します。
			if (files[i].isFile() && fileName.matches("^[0-9]{8}[.]rcd$")) {

				//trueの場合の処理
				rcdFiles.add(files[i]);
			}
		}
		Collections.sort(rcdFiles);

		for (int i = 0; i < rcdFiles.size() - 1; i++) {
			rcdFiles.get(i);
			rcdFiles.get(i).getName();

			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

			//⽐較する2つのファイル名の先頭から数字の8⽂字を切り出し、int型に変換します。
			if ((latter - former) != 1) {
				System.out.println(UNKNOWN_ERROR);
				return;
			}
		}
		//以上までが該当のファイルを保持にすぎない
		//ここからファイルの取り出し	→ 	読み込み[String型]	→	取り出し	→	型変換
		BufferedReader br = null;

		//取り出し	rcdFilesに複数の売上ファイルの情報を格納しているので、その数だけ繰り返します。
		for (int i = 0; i < rcdFiles.size(); i++) {
			try {
				//売上ファイルを開く準備ができただけ
				File file = new File(args[0], rcdFiles.get(i).getName());

				FileReader fr = new FileReader(file);
				//文字列を受け取るFileReader
				br = new BufferedReader(fr);
				//文字列を受け取って蓄えて、必要な時に渡す

				String line;
				//空の証明
				List<String> fileContents = new ArrayList<>();
				//リストを制作して追加しないと1つ目が２つ目に上書きされてしまう、それゆえ
				//リストにaddして、上書きされないようにする
				while ((line = br.readLine()) != null) {
					//売上ファイルの1行目には支店コード、2行目には売上金額が入っています。
					//1行ずつ読み込まれるため、1ineは1回目は支店コードが入れられる 2回目は売上金額が入る（

					fileContents.add(line);
					//このラインを格納しているlist[fielcontets]にline(支店コード[0]・金額[1])が追加されてく
				}
				if (fileContents.size() != 2) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}
				if (!branchSales.containsKey(fileContents.get(0))) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				String money = fileContents.get(1);

				if (!money.matches("^[0-9]+$")) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				//型の変換 売上ファイルから読み込んだ売上金額をMapに加算していくために、型変換を行う
				long fileSale = Long.parseLong(money);

				//既にMapにある売上⾦額を取得
				Long saleAmount = branchSales.get(fileContents.get(0)) + fileSale;

				//取得したものに加算した売上⾦額「と支店コード」をMapに追加
				branchSales.put(fileContents.get(0), saleAmount);

				if (saleAmount >= 10000000000L) {
					System.out.println("合計⾦額が10桁を超えました");
					return;
				}

			} catch (IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				// ファイルを開いている場合
				if (br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}
		// 支店別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
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
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {

		File file = new File(path, fileName);
		if (!file.exists()) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		}

		BufferedReader br = null;

		try {

			File file1 = new File(path, fileName);
			FileReader fr = new FileReader(file1);
			br = new BufferedReader(fr);

			String line;// 支店定義ファイル(001,大阪支店)
			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {

				String[] items = line.split(",");
				if ((items.length != 2) || (!items[0].matches("^[0-9]{3}$"))) {
					//⽀店定義ファイルの左右両方の仕様が満たされていない場合、
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
				//  金額がまだ入っていない namesにはすでに完成(001,大阪支店)Names（001,0円）上での足し算に生かすためのKEYのみを設定（putで追加=KEYの設定）
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);
				//上の売上ファイル（1行目支店コード、2行目金額）とちがい、支店定義ファイルは「支店コード、金額」なので、1行ずつ読み込んでそれをitemsに入れるだけでよい
			}
		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch (IOException e) {
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
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		//writeFileが普通の字	大枠で書いてある箇所から呼び出されたところ
		BufferedWriter bw = null;
		try {
			//Fileオブジェクトを作成(path=args[]、fileName=branch.out) 名前と場所を指定して
			//コマンドライン引数で指定されたディレクトリに支店別集計ファイルを作成する
			File file = new File(path, fileName);

			//書くための準備
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			//branchNames　keyを取得
			for (String key : branchSales.keySet()) {

				//取った情報 keyをつかってbranchNames,branchSalesの情報を取る
				//branchNames.get(key);//keyを使ってお店の名前が取れました
				//branchSales.get(key);//金額がとる
				bw.write(key + "." + branchNames.get(key) + "," + branchSales.get(key));
				//keyからヴァリューを表現してる　支店番号、支店名、金額
				bw.newLine();
			}

		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}
}
