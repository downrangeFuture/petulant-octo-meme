package com.downrangeproductions.vstargauge;

import android.os.Parcel;
import android.os.Parcelable;

public class Values implements Parcelable {
	
	public float mph,
	             rpm,
	             throttlePos, // 0 to 1
	             vdc; //Should be in true VDC
	public boolean neutral, turnSignal;
	
	private boolean[] bools;
	private static final int NEUTRAL = 0;
	private static final int TURN_SIGNAL = 1;
	
	//Default ctor
	public Values(){
		
	}
	
	//Copy ctor;
	public Values(Parcel pc){
		mph         = pc.readFloat();
		rpm         = pc.readFloat();
		throttlePos = pc.readFloat();
		vdc         = pc.readFloat();
		pc.readBooleanArray(bools);
		
		neutral = bools[NEUTRAL];
		turnSignal = bools[TURN_SIGNAL];
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeFloat(mph);
		dest.writeFloat(rpm);
		dest.writeFloat(throttlePos);
		dest.writeFloat(vdc);
		
		bools = new boolean[2];
		bools[NEUTRAL] = neutral;
		bools[TURN_SIGNAL] = turnSignal;
		
		dest.writeBooleanArray(bools);
	}
	
	public static final Parcelable.Creator<Values> CREATOR = new Parcelable.Creator<Values>() {
		public Values createFromParcel(Parcel pc){
			return new Values(pc);
		}
		
		public Values[] newArray(int size){
			return new Values[size];
		}
	};

}
