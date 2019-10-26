package io.jpress.web.front;

import com.jfinal.aop.Inject;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import io.jboot.db.model.Columns;
import io.jboot.web.controller.annotation.RequestMapping;
import io.jboot.web.validate.EmptyValidate;
import io.jboot.web.validate.Form;
import io.jpress.model.User;
import io.jpress.model.UserAddress;
import io.jpress.service.UserAddressService;
import io.jpress.web.base.UcenterControllerBase;

import java.util.Date;
import java.util.List;
import java.util.Set;


@RequestMapping(value = "/ucenter/address", viewPath = "/WEB-INF/views/ucenter/address")
public class AddressController extends UcenterControllerBase {

    @Inject
    private UserAddressService userAddressService;

    /**
     * 用户地址列表
     */
    public void index() {

        Page<UserAddress> page = userAddressService.paginate(getPagePara(), 10);
        setAttr("page", page);
        render("address_list.html");
    }

    /**
     * 用户地址新增，编辑
     */
    public void edit() {
        Long id = getParaToLong("id");
        if (id!=null) {
            UserAddress data = userAddressService.findById(id);
            setAttr("data", data);
        }
        render("address_edit.html");
    }

    /**
     * 选择地址的弹出层
     */
    public void layer(){
        Page<UserAddress> page = userAddressService.paginate(getPagePara(), 10);
        setAttr("page", page);
        render("address_layer.html");
    }


    /**
     * 批量删除
     */
    @EmptyValidate(@Form(name = "ids"))
    public void doDelByIds() {
        Set<String> idsSet = getParaSet("ids");

        User user = getLoginedUser();

        //in 发现有点问题，后续解决
        for (String s : idsSet) {
            Columns columns = Columns.create();
            columns.eq("user_id",user.getId());
            columns.eq("id", s);
            userAddressService.deleteByColumns(columns);
        }

        renderJson(Ret.ok());
    }

    /**
     * 单个删除
     */
    public void doDel() {
        Long id = getIdPara();
        Columns columns = Columns.create();
        User user = getLoginedUser();
        columns.add("user_id",user.getId());
        columns.add("id",id);
        renderJson(userAddressService.deleteByColumns(columns) ? Ret.ok() : Ret.fail());
    }

    /**
     * 新增/编辑地址
     */
    public void doAdd() {

        UserAddress address = getBean(UserAddress.class,"address");

        User user = getLoginedUser();
        address.setUserId(user.getId());
        address.setCreated(new Date());

        if (address.getId() != null) {
            address.setModified(new Date());
        }

        //新设置了默认，那么其他地址改为非默认
        if(address.isDefault()){
            Columns columns = Columns.create();
            columns.add("user_id",user.getId());
            columns.eq("is_default",true);
            List<UserAddress> list = userAddressService.findListByColumns(columns);
            if(list!=null&&list.size()>0){
                for (UserAddress userAddress : list) {
                    userAddress.setWidthDefault(false);
                    userAddress.update();
                }
            }
        }

        userAddressService.saveOrUpdate(address);

        renderJson(Ret.ok());
    }

}
