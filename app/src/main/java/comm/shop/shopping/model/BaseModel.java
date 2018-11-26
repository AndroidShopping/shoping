package comm.shop.shopping.model;

import cn.droidlover.xdroidmvp.net.IModel;

/**
 * Created by wanglei on 2016/12/11.
 */

public class BaseModel implements IModel {
    protected int status;
    public String message;


    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean isAuthError() {
        return false;
    }

    @Override
    public boolean isBizError() {
        return status != 0;
    }

    @Override
    public String getErrorMsg() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
