using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class RotateCube : MonoBehaviour {
	public float speed = 30f;

	// Use this for initialization
	void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {
		transform.Rotate(speed*Time.deltaTime, 2*speed*Time.deltaTime,-speed*Time.deltaTime);
	}
}
