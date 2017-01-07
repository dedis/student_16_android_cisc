package ch.epfl.dedis.net;

import android.util.Log;

import ch.epfl.dedis.crypto.Ed25519;
import ch.epfl.dedis.crypto.Utils;
import com.google.gson.annotations.SerializedName;

import net.i2p.crypto.eddsa.EdDSAEngine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;

public class SkipBlock {

    private static final String TAG = "net.SkipBlock";

    @SerializedName("SkipBlockFix")
    SkipBlockFix mFix;

    @SerializedName("Hash")
    String mId;

    @SerializedName("Sig")
    String mSig;

    @SerializedName("Msg")
    String mMsg;

    public SkipBlockFix getFix() {
        return mFix;
    }

    public String getId() {
        return mId;
    }

    public boolean verifyBlock() {
        return verifyHash() && verifySignature();
    }

    public boolean verifyHash() {
        Log.d(TAG, "Verify hash.");
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(mFix.mIndex);
            sha256.update(buffer.array());

            buffer = ByteBuffer.allocate(8);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(mFix.mHeight);
            sha256.update(buffer.array());

            buffer = ByteBuffer.allocate(8);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(mFix.mMaximumHeight);
            sha256.update(buffer.array());

            buffer = ByteBuffer.allocate(8);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(mFix.mBaseHeight);
            sha256.update(buffer.array());

            for (String s : mFix.mBackLinkIds) {
                sha256.update(Utils.decodeBase64(s));
            }

            sha256.update(Utils.decodeBase64(mFix.mVerifierId));
            sha256.update(Utils.decodeBase64(mFix.mParentBlockId));
            sha256.update(Utils.decodeBase64(mFix.mAggregate));
            sha256.update(Utils.decodeBase64(mFix.mAggregateResp));
            sha256.update(Utils.decodeBase64(mFix.mData));

            byte[] hash = sha256.digest();
            return Arrays.equals(hash, Utils.decodeBase64(mId));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean verifySignature() {
        Log.d(TAG, "Verify signature.");
        byte[] aggregate = Utils.decodeBase64(mFix.mAggregate);
        PublicKey pb = Ed25519.BytesToPub(aggregate);

        EdDSAEngine engine = new EdDSAEngine();

        try {
            byte[] signature = Utils.decodeBase64(mSig);
            byte[] message = Utils.decodeBase64(mMsg);

            engine.initVerify(pb);

            byte[] iii = Arrays.copyOfRange(signature, 0, 64);
            return engine.verifyOneShot(message, iii);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
