package com.kongapi.mapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * @author Darth Revan
 *
 */
public class KongClient {

	private final static Logger LOGGER = Logger.getLogger(KongClient.class.getName());

	@SuppressWarnings("deprecation")

	//private HttpClient client = new DefaultHttpClient();


//	public DefaultHttpClient client()  {
//
//	    DefaultHttpClient client = new DefaultHttpClient();
//	    ClientConnectionManager mgr = client.getConnectionManager();
//	    HttpParams params = client.getParams();
//	    client = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
//
//	            mgr.getSchemeRegistry()), params);
//	    return client;
//	}

	CloseableHttpClient httpClient = HttpClients.createDefault();


	public String getRequest(String url) throws Exception {

		StringBuffer result = null;

		try {
			HttpGet request = new HttpGet(url);

			HttpResponse response = httpClient.execute(request);

			LOGGER.info("\nSending 'GET' request to URL : " + url);
			LOGGER.info("Response Code : "
					+ response.getStatusLine().getStatusCode());

			BufferedReader rd = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));
			result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			EntityUtils.consume(response.getEntity());

		}catch (Exception e){
			e.printStackTrace();
		}
		return result.toString();
	}

	/**
	 *
	 * @param url
	 * @param jsonObj
	 * @throws Exception
	 */
	public void postRequest(String url, JSONObject jsonObj)
			throws Exception {

		try {
			String apiName = jsonObj.getString("name").toString();
			LOGGER.info("Registering api " + apiName);

			HttpPost post = new HttpPost(url);

			StringEntity data = new StringEntity(jsonObj.toString());
			data.setContentType("application/json");
			post.setEntity(data);

			HttpResponse response = httpClient.execute(post);

			int statusCode = response.getStatusLine().getStatusCode();

			LOGGER.info("\nSending 'POST' request to Kong Admin endpoint : "
					+ url);
			LOGGER.info("Post parameters : " + post.getEntity());
			LOGGER.info("Response Code : "
					+ response.getStatusLine().getStatusCode());

			switch (statusCode) {
			case 201:
				LOGGER.info("Registered api "
						+ jsonObj.getString("name").toString());
				break;
			case 409:
				LOGGER.info("409 Conflict, Already exit");
				break;
			default:
				throw new RuntimeException("Failed : HTTP error code : "
						+ statusCode);


			}
			 BufferedReader rd = new BufferedReader(new InputStreamReader(response
			 .getEntity().getContent()));
			 String line = "";
			 while ((line = rd.readLine()) != null) {
			 LOGGER.info(line);
			 }
			 EntityUtils.consume(response.getEntity());

		}catch(Exception e){
		e.printStackTrace();
		}

	}

	/**
	 *
	 * @param url
	 * @param api
	 * @throws HttpException
	 * @throws IOException
	 */

	public void deleteRequest(String url, String api) throws HttpException,
			IOException {

		System.out.println(url + "/" + api);
		try {
			HttpDelete httpdelete = new HttpDelete(url + "/" + api);
			HttpResponse response = httpClient.execute(httpdelete);
			switch (response.getStatusLine().getStatusCode()) {
			case 204:
				LOGGER.info("Delete Successfully");
				break;
			case 404:
				LOGGER.info("Api " + api + " Not found");
			default:
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			EntityUtils.consume(response.getEntity());

		}catch(Exception e){
			e.printStackTrace();
		}

	}

	/**
	 *
	 * @param url
	 * @param jsonObj
	 * @param name
	 * @throws HttpException
	 * @throws IOException
	 */

	public void patchRequest(String url, JSONObject jsonObj, String name)
			throws HttpException, IOException {

		HttpPatch patch = new HttpPatch(url + "/" + name);

		try {
			StringEntity data = new StringEntity(jsonObj.toString());
			data.setContentType("application/json");
			patch.setEntity(data);
			HttpResponse response = httpClient.execute(patch);
			LOGGER.info("\nSending 'Patch' request to Kong Admin endpoint : "
					+ url);
			LOGGER.info("Post parameters : " + patch.getEntity());
			LOGGER.info("Response Code : "
					+ response.getStatusLine().getStatusCode());

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}

			BufferedReader rd = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				LOGGER.info(line);
			}
			EntityUtils.consume(response.getEntity());


		}catch(Exception e) {
			e.printStackTrace();

		}
	}


}
