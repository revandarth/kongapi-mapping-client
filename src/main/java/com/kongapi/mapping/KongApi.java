package com.kongapi.mapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.oneidentity.kong.*;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author Darth Revan
 *
 */

public class KongApi {

	private final static Logger LOGGER = Logger.getLogger(KongApi.class.getName());

	private final static String url = "http://localhost:8001";



	public static void main(String[] args) throws JSONException, Exception {


	    if (args[0] == null || args[0].trim().isEmpty()) {
	        System.out.println("You need to pass json file name to compare!");
	        return;
	    } else {
	    	System.out.println("Reading json file "+args[0]);
	    }

		KongClient client = new KongClient();


		List<Object> updateList = new ArrayList<Object>();
		List<Object> ignoreList = new ArrayList<Object>();
		List<Object> deletedList = new ArrayList<Object>();
		List<Object> addList = new ArrayList<Object>();


		try {

			LOGGER.info("Getting Json data from Kong API");

//			later
//			String filename = args[0];
//			System.out.println(filename);
			File apiJSONFile = new File(args[0]);

			String apisFromJsonFile = FileUtils.readFileToString(apiJSONFile, "utf-8");


			JSONObject jsonObjsFromJSONFile = new JSONObject(apisFromJsonFile);

			JSONObject jsonObjsFromKongApi = new JSONObject(client.getRequest(url+"/apis"));
			LOGGER.info("From Json File: " + jsonObjsFromJSONFile);
			LOGGER.info("From kong api: " + jsonObjsFromKongApi);

			JSONArray jsonFileData = jsonObjsFromJSONFile.getJSONArray("data");
			JSONArray kongApiData = jsonObjsFromKongApi.getJSONArray("data");

			LOGGER.info("Verifying API lists from JSON File with Kong API ");
			for (int i = 0; i < jsonFileData.length(); i++) {


				//bitBucketList.add(jsonFileData.getJSONObject(i));

				JSONObject jsonObjFromJSONFile = jsonFileData.getJSONObject(i);
				String nameFromJSONFile = jsonObjFromJSONFile.getString("name").toString();

				LOGGER.info("Api name from json file: "+nameFromJSONFile);

				boolean available = false;
				for (int j = 0; j < kongApiData.length(); j++) {
					JSONObject jsonObjFromKongApi = kongApiData.getJSONObject(j);
					String nameFromKongApi = jsonObjFromKongApi.getString("name").toString();
					LOGGER.info("Api name from Kong api: " + nameFromKongApi);

					if (nameFromJSONFile.equals(nameFromKongApi)) {
						LOGGER.info("API is already exist, going to compare the data");

						if (compareJson(jsonObjFromJSONFile, jsonObjFromKongApi)) {
							LOGGER.info("Looks similar, ignoring");
							ignoreList.add(jsonFileData.getJSONObject(i));

						} else {
							updateList.add(jsonFileData.getJSONObject(i));
							client.patchRequest(url+"/apis", jsonFileData.getJSONObject(i),
									nameFromJSONFile);

						}
						available = true;
						break;
					}
				}
				if (!available) {
					addList.add(jsonFileData.getJSONObject(i));
					client.postRequest(url+"/apis", jsonFileData.getJSONObject(i));
				}
			}
			deleteList(jsonFileData, kongApiData, client);
			System.out.println("Updatelist: " + updateList);
			System.out.println("Ignore List: " + ignoreList);
			System.out.println("Add List: " + addList);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param jsonFileObj
	 * @param kongApiObj
	 * @return
	 */
	private static boolean compareJson(JSONObject jsonFileObj, JSONObject kongApiObj){

		// Currently we are comparing only upstream_url, methods, uris

		System.out.println("Comparing JSONObjects data");

		if (!jsonFileObj.getString("upstream_url").toString()
				.equals(kongApiObj.getString("upstream_url").toString())) {
			LOGGER.info("upstream url is changed");
			LOGGER.info("upstream_url from jsonFile: "
					+ jsonFileObj.getString("upstream_url").toString());
			LOGGER.info("upstream_url from kong: "
					+ kongApiObj.getString("upstream_url").toString());
			return false;
		}

		List<Object> methodsFromJSONFile = jsonFileObj.getJSONArray("methods")
				.toList();
		List<Object> kongMethods = kongApiObj.getJSONArray("methods").toList();

		if (!isListEquals(methodsFromJSONFile, kongMethods)) {
			System.out.println("Methods are not Equal");
			return false;
		}

		List<Object> jsonFileURI = jsonFileObj.getJSONArray("uris").toList();
		List<Object> kongURI = kongApiObj.getJSONArray("uris").toList();

		if (!isListEquals(jsonFileURI, kongURI)) {
			System.out.println("uris not are same");
			return false;
		}

		return true;
	}

	/**
	 *
	 * @param l1
	 * @param l2
	 * @return
	 */

	public static <T> boolean isListEquals(List<T> l1, List<T> l2) {
		final Set<T> s1 = new HashSet<T>(l1);
		final Set<T> s2 = new HashSet<T>(l2);
		return s1.equals(s2);
	}

	/**
	 *
	 * @param fromJsonFile
	 * @param fromKongApi
	 * @param client
	 * @throws HttpException
	 * @throws IOException
	 */

	public static void deleteList(JSONArray fromJsonFile, JSONArray fromKongApi, KongClient client) throws IOException {

		List<Object> delList = new ArrayList<Object>();
		for (int i = 0; i < fromKongApi.length(); i++) {

			JSONObject kongApiObj = fromKongApi.getJSONObject(i);
			String nameFromKong = kongApiObj.getString("name").toString();

			LOGGER.info("Name From Kong: " + nameFromKong);

			boolean available = false;
			for (int j = 0; j < fromJsonFile.length(); j++) {
				JSONObject fileObj = fromJsonFile.getJSONObject(j);
				String nameFromJsonFile = fileObj.getString("name").toString();
				LOGGER.info("Name From Json File: " + nameFromJsonFile);

				if (nameFromJsonFile.equals(nameFromKong)) {
					LOGGER.info("I am here and there");
					available = true;
					break;
				}
			}
			if (!available) {
				LOGGER.info("Not qualified, I am kicking off myself");
				try {
					client.deleteRequest(url+"/apis", nameFromKong);
				} catch (HttpException e) {
					e.printStackTrace();
				}
				delList.add(fromKongApi.getJSONObject(i));
				LOGGER.info("Added " + nameFromKong + " api delete list");
			}
		}
	System.out.println("Delete List: "+delList);

	}


}
