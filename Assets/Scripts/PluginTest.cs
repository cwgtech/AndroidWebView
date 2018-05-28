using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class PluginTest : MonoBehaviour {

	const string pluginName = "com.cwgtech.unity.MyPlugin";

	class AlertViewCallback : AndroidJavaProxy
	{
		private System.Action<int> alertHandler;

		public AlertViewCallback(System.Action<int>alertHandlerIn) : base (pluginName + "$AlertViewCallback")
		{
			alertHandler = alertHandlerIn;
		}
		public void onButtonTapped(int index)
		{
			Debug.Log("Button tapped: " + index);
			if (alertHandler != null)
				alertHandler(index);
		}
	}

	class ShareImageCallback: AndroidJavaProxy
	{
		private System.Action<int> shareHandler;
		public ShareImageCallback(System.Action<int>shareHandlerIn) : base (pluginName + "$ShareImageCallback")
		{
			shareHandler = shareHandlerIn;
		}
		public void onShareComplete(int result)
		{
			Debug.Log("ShareComplete:" + result);
			isSharingScreenShot = false;
			if (shareHandler != null)
				shareHandler(result);
		}
	}

	static AndroidJavaClass _pluginClass;
	static AndroidJavaObject _pluginInstance;

	public Button shareButton;
	public Text timeStamp;
	public RectTransform webPanel;
	public RectTransform buttonStrip;

	static bool isSharingScreenShot;

	public static AndroidJavaClass PluginClass
	{
		get {
			if (_pluginClass==null)
			{
				_pluginClass = new AndroidJavaClass(pluginName);
				AndroidJavaClass playerClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
				AndroidJavaObject activity = playerClass.GetStatic<AndroidJavaObject>("currentActivity");
				_pluginClass.SetStatic<AndroidJavaObject>("mainActivity",activity);
			}
			return _pluginClass;
		}
	}

	public static AndroidJavaObject PluginInstance
	{
		get {
			if (_pluginInstance==null)
			{
				_pluginInstance = PluginClass.CallStatic<AndroidJavaObject>("getInstance");
			}
			return _pluginInstance;
		}
	}

	// Use this for initialization
	void Start () {

		Debug.Log("Elapsed Time: " + getElapsedTime());
		if (timeStamp != null)
			timeStamp.gameObject.SetActive(false);
		
	}


	double getElapsedTime()
	{
		if (Application.platform == RuntimePlatform.Android)
			return PluginInstance.Call<double>("getElapsedTime");
		Debug.LogWarning("Wrong platform");
		return 0;
	}

	void showAlertDialog(string[] strings, System.Action<int>handler = null)
	{
		if (strings.Length<3)
		{
			Debug.LogError("AlertView requires at least 3 strings");
			return;
		}

		if (Application.platform == RuntimePlatform.Android)
			PluginInstance.Call("showAlertView", new object[] { strings, new AlertViewCallback(handler) });
		else
			Debug.LogWarning("AlertView not supported on this platform");
	}

	public void ShareButtonTapped()
	{
		if (shareButton != null)
			shareButton.gameObject.SetActive(false);
		if (timeStamp!=null)
		{
			timeStamp.text = System.DateTime.Now.ToString("MM/dd/yyyy HH:mm:ss");
			timeStamp.gameObject.SetActive(true);
		}
		ShareScreenShot(Application.productName + " screenshot", (int result) => {
			Debug.Log("Share complete with: " + result);
			showAlertDialog(new string[] { "Share Complete", "Share completed with: " + result, "OK" });
			if (shareButton != null)
				shareButton.gameObject.SetActive(true);
			if (timeStamp != null)
				timeStamp.gameObject.SetActive(false);
		});
	}

	public void ShareScreenShot(string caption, System.Action<int> shareComplete)
	{
		if (isSharingScreenShot)
		{
			Debug.LogError("Already sharing screenshot - aborting");
			return;
		}
		isSharingScreenShot = true;
		StartCoroutine(waitForEndOfFrame(caption, shareComplete));
	}

	IEnumerator waitForEndOfFrame(string caption, System.Action<int> shareComplete)
	{
		yield return new WaitForEndOfFrame();
		Texture2D image = ScreenCapture.CaptureScreenshotAsTexture();
		Debug.Log("Image size: " + image.width + " x " + image.height);
		byte[] imagePNG = image.EncodeToPNG();
		Debug.Log("PNG size: " + imagePNG.Length);
		if (Application.platform == RuntimePlatform.Android)
		{
			PluginInstance.Call("shareImage", new object[] { imagePNG, caption, new ShareImageCallback(shareComplete) });
		}
		Object.Destroy(image);
	}

	public void OpenWebView(string URL, int pixelShift)
	{
		if (Application.platform == RuntimePlatform.Android)
			PluginInstance.Call("showWebView", new object[] { URL, pixelShift });
	}

	public void CloseWebView(System.Action<int> closeComplete)
	{
		if (Application.platform == RuntimePlatform.Android)
			PluginInstance.Call("closeWebView", new object[] { new ShareImageCallback(closeComplete) });
		else
			closeComplete(0);
	}

	public void OpenWebViewTapped()
	{
		Canvas parentCanvas = buttonStrip.GetComponentInParent<Canvas>();
		int stripHeight = (int)(buttonStrip.rect.height * parentCanvas.scaleFactor + 0.5f);
		webPanel.gameObject.SetActive(true);
		OpenWebView("http://www.cwgtech.com", stripHeight);
	}


	public void CloseWebViewTapped()
	{
		CloseWebView((int result) =>
		{
			webPanel.gameObject.SetActive(false);
		});
	}

}
