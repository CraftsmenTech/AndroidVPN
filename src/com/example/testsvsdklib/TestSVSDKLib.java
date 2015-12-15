package com.example.testsvsdklib;

import java.util.ArrayList;
import java.util.HashMap;

import topsec.sslvpn.svsdklib.SVSDKLib;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TestSVSDKLib extends Activity implements OnClickListener {
	private static final String TAG = "TestSVSDKLib";

	public static final int VPN_MSG_STATUS_UPDATE = 100; // VPN状态通知消息号
	public static final int QUERY_VPN_MSG_STATUS_UPDATE = 101; // VPN状态通知消息号

	// VPN服务器地址、端口号、用户名、密码
		private final String VPN_SERVER = "59.49.15.130";
		private final int VPN_PORT = 443;
		private final String VPN_USERNAME = "oa";
		private final String VPN_PASSWORD = "123456";

	private Dialog mShowingDialog;

	public Handler MsgHandler = new Handler() {
		// 处理具体的message,该方法由父类中进行继承.
		@Override
		public void handleMessage(Message msg) {
			int msgID = msg.what;

			Bundle bundle = (Bundle) msg.obj;

			switch (msg.what) {
			case VPN_MSG_STATUS_UPDATE: // VPN库发送消息处理
			{
				if (null != bundle) {
					String vpnStatus = bundle.getString("vpnstatus");
					String vpnErr = bundle.getString("vpnerror");
					
					if (vpnStatus.equalsIgnoreCase("6")) {
						// VPN隧道建立成功
						Log.i(TAG, "VPN库消息通知：VPN隧道建立成功");
						// http://127.0.0.1:30080/cctv2/Jhsoft.mobileapp/login/loginbyurl.html?userName=trx&pwd=111111

						Toast.makeText(TestSVSDKLib.this, "VPN库消息通知：VPN隧道建立成功",
								Toast.LENGTH_SHORT).show();
					}

					if (vpnStatus.equalsIgnoreCase("200")) {
						Toast.makeText(TestSVSDKLib.this, "VPN库消息通知：VPN隧道超时",
								Toast.LENGTH_SHORT).show();
					}

					if (!vpnErr.equalsIgnoreCase("0")) {

						if (vpnErr.equalsIgnoreCase("10")) {
							Log.i(TAG,
									"VPN库消息通知：VPN需要重新登陆，可提示用户进行选择是否踢出上一个用户，现在是强行踢出上一个用户");
							SVSDKLib vpnlib = SVSDKLib.getInstance();
							vpnlib.reLoginVPN();

						} else {
							// VPN隧道建立出错
							Toast.makeText(TestSVSDKLib.this,
									"VPN库消息通知：当前VPN错误为：" + vpnErr,
									Toast.LENGTH_SHORT).show();

						}
					}
				}
			}
				break;
			case QUERY_VPN_MSG_STATUS_UPDATE: // 查询线程消息处理
			{
				if (null != bundle) {
					String vpnStatus = bundle.getString("vpnstatus");
					String vpnErr = bundle.getString("vpnerror");
					if (vpnStatus.equalsIgnoreCase("6")) {
						// VPN隧道建立成功
						Toast.makeText(TestSVSDKLib.this, "查询线程通知：VPN隧道建立成功",
								Toast.LENGTH_SHORT).show();
					}

					if (!vpnErr.equalsIgnoreCase("0")) {
						// VPN隧道建立出错
						Toast.makeText(TestSVSDKLib.this,
								"查询线程通知：当前VPN错误为：" + vpnErr, Toast.LENGTH_SHORT)
								.show();
					}
				}
			}
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		findviewByid();

		// 初始化VPNSDK库
		InitSVSDKLib();

	}

	private void findviewByid() {

		Button btnStart = (Button) findViewById(R.id.BTN_START);
		Button btnStop = (Button) findViewById(R.id.BTN_STOP);
		Button btnConnectSvr = (Button) findViewById(R.id.BTN_GETVPNSTATUS);


		btnStart.setOnClickListener(this);
		btnStop.setOnClickListener(this);
		btnConnectSvr.setOnClickListener(this);

	}

	// 初始化VPN库
	private void InitSVSDKLib() {

		// 获取VPN库实例
		SVSDKLib vpnlib = SVSDKLib.getInstance();

		// 设置VPN客户端的释放目录
		Context appContext = getApplicationContext();
		vpnlib.setSVClientPath(appContext.getFilesDir().getPath());

		// 设置应用程序的资产管理器
		vpnlib.setAppam(this.getAssets());

		
				
		// 设置VPNSDK库的消息处理器
		vpnlib.setMsgHandler(MsgHandler);

		// 设置VPNSDK库的VPN状态变更消息号
		vpnlib.setVPNMsgID(VPN_MSG_STATUS_UPDATE);

		// 设置VPN连接信息
		vpnlib.setVPNInfo(VPN_SERVER, VPN_PORT, VPN_USERNAME, VPN_PASSWORD);

		// VPN客户端连接前的准备
		vpnlib.prepareVPNSettings();

		Log.i(TAG, "InitSVSDKLib done");
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.BTN_START: {
			doStart();

			break;
		}

		case R.id.BTN_STOP: {
			doStop();

			break;
		}

		case R.id.BTN_GETVPNSTATUS: {
			doGetVPNStatus();

			break;
		}

		}
	}

	private void showErrorDialog(String sErrInfo) {

		mShowingDialog = new AlertDialog.Builder(this).setTitle("错误")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(sErrInfo)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						return;
					}
				}).create();
		mShowingDialog.show();
		return;
	}

	// 启动VPN连接
	private void doStart() {
		Log.i(TAG, "start vpn");
		SVSDKLib vpnlib = SVSDKLib.getInstance();

		// 获取参数
		EditText edtIP = (EditText) findViewById(R.id.edtip);
		EditText edtPort = (EditText) findViewById(R.id.edtport);
		EditText edtUname = (EditText) findViewById(R.id.edtuname);
		EditText edtUpwd = (EditText) findViewById(R.id.edtupwd);

		String sIP, sUname, sUpwd;
		int port;

		sIP = edtIP.getText().toString();
		sUname = edtUname.getText().toString();
		sUpwd = edtUpwd.getText().toString();

		if (sIP == null || sIP.length() == 0) {
			showErrorDialog("VPN地址不能为空");
			return;
		}

		try {
			port = Integer.parseInt(edtPort.getText().toString());
		} catch (NumberFormatException e) {
			showErrorDialog("VPN端口有效范围是1-65535");
			return;
		}

		if ((port <= 0) || (port > 65535)) {
			showErrorDialog("VPN端口有效范围是1-65535");
			return;
		}

		if (sUname == null || sUname.length() == 0) {
			showErrorDialog("用户名不能为空");
			return;
		}
		if (sUpwd == null || sUpwd.length() == 0) {
			showErrorDialog("用户密码不能为空");
			return;
		}

		// vpnlib.setVPNInfo("192.168.95.84", 443, "1", "111111");
		vpnlib.setVPNInfo(sIP, port, sUname, sUpwd);
		Log.i("ttt", "ip= " + sIP + " port= " + port + " uname= " + sUname);
		vpnlib.prepareVPNSettings();
		// 获取VPN库实例
		vpnlib.stopVPN();
		// 启动VPN连接
		vpnlib.startVPN();
		// 启动一个查询线程，主动查询VPN状态，VPN成功后
		// 查询线程会给UI主线程发送消息
		// 若VPNSDK库初始化时设置了MsgHandler和MSGID
		// 可由VPNSDK库来发送通知，此线程可不必开启
//		 new GetVPNStatusThread().start();

	}

	// 关闭VPN连接
	private void doStop() {
		// 停止VPN连接
		SVSDKLib vpnlib = SVSDKLib.getInstance();
		vpnlib.stopVPN();
	}

	// 获取VPN状态
	private void doGetVPNStatus() {
		// 获取VPN状态
		String sVPNStatus;
		SVSDKLib vpnlib = SVSDKLib.getInstance();
		sVPNStatus = vpnlib.getVPNStatus();
		
		Toast.makeText(TestSVSDKLib.this, "当前VPN状态为：" + sVPNStatus,
				Toast.LENGTH_SHORT).show();
		
		ArrayList<HashMap<String, String>> reslist = vpnlib.getResList();
		Log.i("test", "port1 :" + vpnlib.getResLocalPort("59.49.15.130", 443));
		Log.i("test", "port2 :" + vpnlib.getResLocalPort("59.49.15.130", 443));

	}

	// VPN状态查询线程
//	private class GetVPNStatusThread extends Thread {
//		// @Override
//		public void run() {
//			String sVPNStatus;
//			String sVPNErr;
//			SVSDKLib vpnlib = SVSDKLib.getInstance();
//
//			int nTimeOut = 10; // 超时时间，10秒
//			int nTimeStep = 0;
//
//			try {
//				while (nTimeStep < nTimeOut) {
//					sVPNStatus = vpnlib.getVPNStatus();
//					sVPNErr = vpnlib.getVPNError();
//					if (sVPNStatus.equalsIgnoreCase("6")) {
//						// 通知主进行VPN连接成功
//						Message msg = Message.obtain(MsgHandler);
//						msg.what = QUERY_VPN_MSG_STATUS_UPDATE;
//
//						sVPNStatus = "6";
//
//						Bundle bundle = new Bundle();
//						bundle.putString("vpnerror", sVPNErr);
//						bundle.putString("vpnstatus", sVPNStatus);
//
//						msg.obj = bundle;
//						msg.sendToTarget();
//						break;
//					}
//
//					if (!sVPNErr.equalsIgnoreCase("0")) {
//						// 通知主进行VPN连接出错了
//						Message msg = Message.obtain(MsgHandler);
//						msg.what = QUERY_VPN_MSG_STATUS_UPDATE;
//
//						Bundle bundle = new Bundle();
//						bundle.putString("vpnerror", sVPNErr);
//						bundle.putString("vpnstatus", sVPNStatus);
//
//						
//						
//						msg.sendToTarget();
//						msg.obj = bundle;
//						break;
//					}
//
//				}
//				Thread.sleep(1000);
//				nTimeStep++;
//
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			return;
//		}
//	}

}