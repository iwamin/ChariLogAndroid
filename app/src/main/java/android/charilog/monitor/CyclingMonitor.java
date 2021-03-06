package android.charilog.monitor;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class CyclingMonitor {
	private final int SPEED_MEASURE_RANGE = 5;

	private static CyclingMonitor _instance;

	@Getter
	private CyclingInfo info = new CyclingInfo();

	private List<Location> locationList = new ArrayList<>();

	@Getter
	@Setter
	private float accuracy;

	private CyclingMonitor() {
	}

	public synchronized static CyclingMonitor getInstance() {
		if (_instance == null) {
			_instance = new CyclingMonitor();
		}
		return _instance;
	}

	public synchronized void reset() {
		locationList.clear();
		info.reset();
	}

	public synchronized void reportLocationChange(Location location) {
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
			info.averageSpeed = 3600.0 * info.totalDistance / info.totalTime;
		}

		// 新しい位置をリストに登録
		info.location = location;
		locationList.add(location);

		if (locationList.size() > SPEED_MEASURE_RANGE) {
			locationList.remove(0);
		}

		Log.v("CyclingMonitor", info.toString());
	}

	private double calculateSpeed(Location current) {
		// 現在の速度を算出する
		double speed = 0;
		int backNum = SPEED_MEASURE_RANGE - 1;	// {backNum}個前の値と比較する

		if (locationList.size() >= backNum) {
			Location base = locationList.get(locationList.size() - backNum);
			speed = 3600.0 * calculateDistance(base, current) / (current.getTime() - base.getTime());
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
		@Getter
		Location location;		// 現在の位置情報
		@Getter
		long startTime;			// 記録開始時刻[msec]
		@Getter
		long totalTime;			// 経過時間[msec]
		@Getter
		int totalDistance;		// トータル移動距離[m]
		@Getter
		double averageSpeed;	// 平均速度[km/h]
		@Getter
		double currentSpeed;	// 現在の速度[km/h]
		@Getter
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
