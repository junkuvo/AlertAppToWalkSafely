package apps.junkuvo.alertapptowalksafely;

import android.hardware.SensorEvent;
import android.util.Log;

import java.util.Date;

/**
 * 家族度センサの値から、歩行を判断し、カウントを返す
 */
public class WalkCountCalculator {

    private float oldx = 0f;
    private float oldy = 0f;
    private float oldz = 0f;

    private float dx = 0f;
    private float dy = 0f;
    private float dz = 0f;

    // 重複カウント防止用フラグ
    boolean isCountable = true;
    // 歩数カウンター（蓄積される値）
    int counter = -1;

    // １つ前のベクトル量
    double oldVectorDeltaSize = 0;
    // 現在ベクトル量
    double vectorDeltaSize = 0;
    // ベクトル変化を検知した時間
    long changeTime = 0;

    // 閾値
    double threshold = 15;
    // 軸方向転換がされたとみなす最小閾値
    double thresholdMin = 1;
    // ベクトル変化検出しない時間の閾値
    long thresholdTime = 190;

    // X軸加速方向
    boolean vecx = true;
    // Y軸加速方向
    boolean vecy = true;
    // Z軸加速方向
    boolean vecz = true;
    // 加速度方向の転換回数
    int vecchangecount = 0;

    public float getDx() {
        return dx;
    }

    public float getDy() {
        return dy;
    }

    public float getDz() {
        return dz;
    }

    public double getVectorDelataSize() {
        return vectorDeltaSize;
    }

    public int getCounter() {
        return counter;
    }

    public WalkCountCalculator() {
    }

    public int walkCountCalculate(SensorEvent event) {
        // 増加量
        dx = event.values[0] - oldx;
        dy = event.values[1] - oldy;
        dz = event.values[2] - oldz;
        // ベクトル量をピタゴラスの定義から求める。
        // が正確な値は必要でなく、消費電力から平方根まで求める必要はない
        // vectorSize = Math.sqrt((double)(dx*dx+dy*dy+dz*dz));
        vectorDeltaSize = (double) (dx * dx + dy * dy + dz * dz);

        // ベクトル計算を厳密に行うと計算量が上がるため、簡易的な方向を求める。
        // 一定量のベクトル量があり向きの反転があった場合（多分走った場合）
        // vecchangecountはSENSOR_DELAY_NORMALの場合、200ms精度より
        // 加速度変化が検出できないための専用処理。精度を上げると不要
        // さらに精度がわるいことから、連続のベクトル変化は検知しない。
        long dt = new Date().getTime() - changeTime;
        // 各軸方向の方向転換があったかどうかを判定（方向転換されればtrue）
        boolean dxx = Math.abs(dx) > thresholdMin && vecx != (dx >= 0);
        boolean dxy = Math.abs(dy) > thresholdMin && vecy != (dy >= 0);
        boolean dxz = Math.abs(dz) > thresholdMin && vecz != (dz >= 0);
        if (vectorDeltaSize > threshold && dt > thresholdTime
                && (dxx || dxy || dxz)) {
            vecchangecount++;
            changeTime = new Date().getTime();
        }

        // ベクトル量がある状態で向きが２回（上下運動とみなす）変わった場合
        // または、ベクトル量が一定値を下回った（静止とみなす）場合、カウント許可
        if (vecchangecount > 1 || vectorDeltaSize < 1) {
            isCountable = true;
            vecchangecount = 0;
        }

        // カウント許可で、閾値を超えるベクトル量がある場合、カウントする
        if (isCountable && vectorDeltaSize > threshold) {
            counter++;
            isCountable = false;
            vecchangecount = 0;
            Log.d("onSensorChanged", "counter:" + counter);
        }

        // カウント時の加速度の向きを保存
        vecx = (dx >= 0);
        vecy = (dy >= 0);
        vecz = (dz >= 0);

        // 状態更新
        oldVectorDeltaSize = vectorDeltaSize;

        // 加速度の保存
        oldx = event.values[0];
        oldy = event.values[1];
        oldz = event.values[2];

        // 表示に使うため、念のため初期値-1のままであれば0に変更
        if(counter == -1){
            counter = 0;
        }
        return (counter)/2;
    }
}
