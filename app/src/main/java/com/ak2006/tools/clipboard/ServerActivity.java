package com.ak2006.tools.clipboard;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

public class ServerActivity extends Activity implements OnClickListener {
	private static final int PORT = 8765;
	private MyHTTPD server;
	private Button serverCtrlBtn;
	private TextView textIpaddr;
	private boolean serverRunning;
	private String formattedIpAddress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server);

		serverCtrlBtn = (Button) findViewById(R.id.serverCtrlBtn);
		serverCtrlBtn.setOnClickListener(this);

		textIpaddr = (TextView) findViewById(R.id.ipaddr);

		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
		formattedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
				(ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
				(ipAddress >> 24 & 0xff));

        AsyncTask<Void,Void,String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    return InetAddress.getByName(formattedIpAddress).toString();
                }catch(UnknownHostException uhe){
                    uhe.printStackTrace();
                }
                return "";
            }
        };
        try {
            String val = task.execute().get();
            textIpaddr.setText(val);
        }catch(Exception e){}
	}

	@Override
	public void onClick(View v) {
		controlServer(v.getId(), true);
	}

	private void controlServer(int id, boolean byclick) {
		switch (id) {
		case R.id.serverCtrlBtn:
			if (serverRunning) {
				serverRunning = false;
				server.stop();
				serverCtrlBtn.setText("Start");
				textIpaddr.setText("");
			} else {
				if (!byclick)
					return;
				try {
					server = new MyHTTPD(this);
					server.start();
					displayServerDetails(server);

					serverCtrlBtn.setText("Stop");
					serverRunning = true;
				} catch (IOException e) {
					Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT)
							.show();
					finish();

				}
			}
			break;
		}
	}

	private void displayServerDetails(MyHTTPD server) {
        StringBuilder builder = new StringBuilder();
		builder.append("Please access! http://" + formattedIpAddress + ":"
				+ PORT);
		// builder.append("\n\n");
		// if (server.getServerSocket() != null) {
		// builder.append(server.getServerSocket().getLocalPort());
		// builder.append("\n");
		// builder.append(server.getServerSocket().getInetAddress());
		// }
		textIpaddr.setText(builder.toString());

    }

	@Override
	protected void onPause() {
		super.onPause();
		controlServer(R.id.serverCtrlBtn, false);
	}

	private class MyHTTPD extends NanoHTTPD {
		ServerActivity serverActivity;

		public MyHTTPD(ServerActivity serverActivity) throws IOException {
			super(formattedIpAddress, PORT);
			this.serverActivity = serverActivity;
		}

		@Override
		public Response serve(String uri, Method method,
				Map<String, String> header, Map<String, String> parameters,
				Map<String, String> files) {
			// for (Entry<String, String> kv : header.entrySet())
			// buf.append(kv.getKey() + " : " + kv.getValue() + "\n");
			// handler.post(new Runnable() {
			// @Override
			// public void run() {
			// hello.setText(buf);
			// }
			// });
			// check for the url
			// get List of Beacon's and return back

			JSONObject devices = new JSONObject();
			JSONArray deviceArr = new JSONArray();
			Response response = null;

			try {
				devices.put("devices", deviceArr);
					JSONObject d = new JSONObject();
					d.put("name", "hi");
					deviceArr.put(d);
				response = new NanoHTTPD.Response(devices.toString());

			} catch (JSONException e) {
				Toast.makeText(serverActivity, e.getMessage(),
						Toast.LENGTH_SHORT).show();
				response = new NanoHTTPD.Response(e.getMessage());

			}
			response.addHeader("Access-Control-Allow-Origin", "*");
			response.addHeader("Access-Control-Allow-Methods", "GET");
			response.addHeader("Access-Control-Allow-Headers", " Content-Type");

			return response;
		}
	}

}
