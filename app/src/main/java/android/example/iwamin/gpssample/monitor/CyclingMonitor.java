package android.example.iwamin.gpssample.monitor;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;

public class CyclingMonitor {
	private final int SPEED_MEASURE_RANGE = 4;

	private static CyclingMonitor _instance;

	private CyclingInfo info = new CyclingInfo();

	private ArrayList<Location> locationList = new ArrayList<>();

	private CyclingMonitor() {
	}

	synchronized public static CyclingMonitor getInstance() {
		if (_instance == null) {
			_instance = new CyclingMonitor();
		}
		return _instance;
	}

	public CyclingInfo getCyclingInfo() {
		return info;
	}

	synchronized public void reset() {
		locationList.clear();
		info.reset();
	}

	synchronized public void reportLocationChange(Location location) {
		if (locationList.isEmpty()) {
			// 初回登録時の時刻を記録する
			info.startTime = location.getTime();
		} else {
			// トータル時間を算出
			info.totalTime = location.getTime() - info.startTime;

			// トータル移動距離を加算
			Location last = locationList.get(locationList.size() - 1);
			info.totalDistance += calculateDistance(last, location);

			// 現在の速度を算出
			info.currentSpeed = calculateSpeed(location);

			// 最高速度を更新する
			if (info.currentSpeed > info.maximumSpeed) {
				info.maximumSpeed = info.currentSpeed;
			}

			// 平均速度を算出
			info.averageSpeed = (double)info.totalDistance / info.totalTime;
		}

		// 新しい位置をリストに登録
		info.location = location;
		locationList.add(location);

		Log.v("CyclingMonitor", info.toString());
	}

	private double calculateSpeed(Location current) {
		// 現在の速度を算出する
		double speed = 0;

		if (locationList.size() >= SPEED_MEASURE_RANGE) {
			Location base = locationList.get(locationList.size() - SPEED_MEASURE_RANGE);
			speed = calculateDistance(base, current) / (current.getTime() - base.getTime());
		}

		return speed;
	}

	private float calculateDistance(Location start, Location end) {
		// 2点間の距離[m]を求める
		float[] result = new float[3];

		Location.distanceBetween(
				start.getLatitude(), start.getLongitude(),
				end.getLatitude(), end.getLongitude(), result);

		return result[0];
	}

	public class CyclingInfo {
		Location location;		// 現在の位置情報
		long startTime;			// 記録開始時刻[msec]
		long totalTime;			// 経過時間[msec]
		int totalDistance;		// トータル移動距離[m]
		double averageSpeed;	// 平均速度[km/h]
		double currentSpeed;	// 現在の速度[km/h]
		double maximumSpeed;	// 最高速度[km/h]

		public void reset() {
			location = null;
			startTime = 0;
			totalTime = 0;
			totalDistance = 0;
			averageSpeed = 0.0;
			currentSpeed = 0.0;
			maximumSpeed = 0.0;
		}

		public double getAverageSpeed() {
			return averageSpeed;
		}

		public double getCurrentSpeed() {
			return currentSpeed;
		}

		public Location getLocation() {
			return location;
		}

		public double getMaximumSpeed() {
			return maximumSpeed;
		}

		public int getTotalDistance() {
			return totalDistance;
		}

		public long getTotalTime() {
			return totalTime;
		}

		@Override
		public String toString() {
			return "CyclingInfo: "
					+ totalTime + "[ms], "
					+ totalDistance + "[m], "
					+ averageSpeed + "[km/h], "
					+ currentSpeed + "[km/h], "
					+ maximumSpeed + "[km/h]";
		}
	}
}
