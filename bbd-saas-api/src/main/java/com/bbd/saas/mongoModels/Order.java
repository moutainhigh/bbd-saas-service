package com.bbd.saas.mongoModels;

import com.bbd.saas.enums.*;
import com.bbd.saas.vo.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Order
 * 订单表
 * Created by luobotao on 2016/4/10.
 */
@Entity("order")
@Indexes(
        @Index(value = "orderNo", fields = @Field("orderNo"))
)
public class Order implements Serializable {
    private static final long serialVersionUID = -6802839001275501390L;
    @Id
    private ObjectId id;
    private ObjectId adminUserId;
    private String mailNum;
    private String orderNo;
    private String areaName;
    private String areaCode;//站点编码
    private String areaRemark;//站点地址
    @Embedded
    private Sender sender;
    @Embedded
    private Reciever reciever;
    private String userId;//派单时的小件员ID
    private Srcs src;
    private OrderStatus orderStatus;
    private ExpressStatus expressStatus;
    private PrintStatus printStatus;
    private int errorFlag;//异常面单？ 0否 1是
    private String errorRemark;//异常信息
    private List<Goods> goods;
    private List<Express> expresses;
    private List<OtherExpreeVO> otherExprees;
    private String rtnReason;//退货原因
    private String rtnRemark;//退货原因备注（退货原因为其他时，此字段不为空）
    private Date dateAplyRtn;//申请退货时间
    private Date orderCreate;//订单创建时间
    private Date orderPay;     //订单支付时间
    private Date dateAdd;
    private Date datePrint;//物流单打印时间
    private Date dateMayArrive;//预计到站时间
    private Date dateArrived;//到站时间
    private Date dateDriverGeted;//司机取货时间
    private Date dateUpd;//
    private SynsFlag synsFlag;//与易普同步状态0未同步 1已同步 2同步失败
    private TransportStatus transportStatus;//运输状态
    private String tradeNo;//商户订单号(我们自己生成的支付订单号)
    private ObjectId uId;//用户ID,网站端进行改版加入账号体系,数据将从User里获取(adminUserId将不再使用)
    private int isRemoved;//是否被移除？ 0：未被移除； 1：被移除
    private String removeReason;//移除原因
    @Transient
    private String parcelCode;
    @Transient
    private String srcMessage;//前台JSP页面中的JS无法根据枚举来获取message
    @Transient
    private String orderStatusMsg;//前台JSP页面中的JS无法根据枚举来获取message
    @Transient
    private UserVO userVO;//传递jsp页面快递员姓名和电话


    public List<OtherExpreeVO> getOtherExprees() {
        return otherExprees;
    }

    public void setOtherExprees(List<OtherExpreeVO> otherExprees) {
        this.otherExprees = otherExprees;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(ObjectId adminUserId) {
        this.adminUserId = adminUserId;
    }

    public String getMailNum() {
        return mailNum;
    }

    public void setMailNum(String mailNum) {
        this.mailNum = mailNum;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getAreaRemark() {
        return areaRemark;
    }

    public void setAreaRemark(String areaRemark) {
        this.areaRemark = areaRemark;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public Reciever getReciever() {
        return reciever;
    }

    public void setReciever(Reciever reciever) {
        this.reciever = reciever;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Srcs getSrc() {
        return src;
    }

    public void setSrc(Srcs src) {
        this.src = src;
    }

    public ExpressStatus getExpressStatus() {
        return expressStatus;
    }

    public void setExpressStatus(ExpressStatus expressStatus) {
        this.expressStatus = expressStatus;
    }

    public PrintStatus getPrintStatus() {
        return printStatus;
    }

    public void setPrintStatus(PrintStatus printStatus) {
        this.printStatus = printStatus;
    }

    public int getErrorFlag() {
        return errorFlag;
    }

    public void setErrorFlag(int errorFlag) {
        this.errorFlag = errorFlag;
    }

    public String getErrorRemark() {
        return errorRemark;
    }

    public void setErrorRemark(String errorRemark) {
        this.errorRemark = errorRemark;
    }

    public List<Goods> getGoods() {
        return goods;
    }

    public void setGoods(List<Goods> goods) {
        this.goods = goods;
    }

    public List<Express> getExpresses() {
        return expresses;
    }

    public void setExpresses(List<Express> expresses) {
        this.expresses = expresses;
    }

    public Date getOrderCreate() {
        return orderCreate;
    }

    public void setOrderCreate(Date orderCreate) {
        this.orderCreate = orderCreate;
    }

    public Date getOrderPay() {
        return orderPay;
    }

    public void setOrderPay(Date orderPay) {
        this.orderPay = orderPay;
    }

    public Date getDateAdd() {
        return dateAdd;
    }

    public void setDateAdd(Date dateAdd) {
        this.dateAdd = dateAdd;
    }

    public Date getDatePrint() {
        return datePrint;
    }

    public void setDatePrint(Date datePrint) {
        this.datePrint = datePrint;
    }

    public Date getDateUpd() {
        return dateUpd;
    }

    public void setDateUpd(Date dateUpd) {
        this.dateUpd = dateUpd;
    }

    public SynsFlag getSynsFlag() {
        return synsFlag;
    }

    public void setSynsFlag(SynsFlag synsFlag) {
        this.synsFlag = synsFlag;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getParcelCode() {
        return parcelCode;
    }

    public void setParcelCode(String parcelCode) {
        this.parcelCode = parcelCode;
    }

    public Date getDateMayArrive() {
        return dateMayArrive;
    }

    public void setDateMayArrive(Date dateMayArrive) {
        this.dateMayArrive = dateMayArrive;
    }

    public Date getDateArrived() {
        return dateArrived;
    }

    public void setDateArrived(Date dateArrived) {
        this.dateArrived = dateArrived;
    }

    public Date getDateDriverGeted() {
        return dateDriverGeted;
    }

    public void setDateDriverGeted(Date dateDriverGeted) {
        this.dateDriverGeted = dateDriverGeted;
    }

    public String getSrcMessage() {
        return src != null ? src.getMessage() : "";
    }
    public UserVO getUserVO() {
        return userVO;
    }

    public void setUserVO(UserVO userVO) {
        this.userVO = userVO;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getRtnReason() {
        return rtnReason;
    }

    public void setRtnReason(String rtnReason) {
        this.rtnReason = rtnReason;
    }

    public Date getDateAplyRtn() {
        return dateAplyRtn;
    }

    public void setDateAplyRtn(Date dateAplyRtn) {
        this.dateAplyRtn = dateAplyRtn;
    }

    public void setSrcMessage(String srcMessage) {
        this.srcMessage = srcMessage;
    }

    public String getRtnRemark() {
        return rtnRemark;
    }

    public void setRtnRemark(String rtnRemark) {
        this.rtnRemark = rtnRemark;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public ObjectId getuId() {
        return uId;
    }

    public void setuId(ObjectId uId) {
        this.uId = uId;
    }

    public String getOrderStatusMsg() {
        return this.orderStatus != null ? this.orderStatus.getMessage() : "";
    }

    public void setOrderStatusMsg(String orderStatusMsg) {
        this.orderStatusMsg = orderStatusMsg;
    }

    public int getIsRemoved() {
        return isRemoved;
    }

    public void setIsRemoved(int isRemoved) {
        this.isRemoved = isRemoved;
    }

    public String getRemoveReason() {
        return removeReason;
    }

    public void setRemoveReason(String removeReason) {
        this.removeReason = removeReason;
    }

    public static String getExpressList(List<Express> expressList) throws JsonProcessingException{

		if(expressList == null){
			return "";
		}
		ObjectMapper mapper = new ObjectMapper(); 
		String json = mapper.writeValueAsString(expressList).replaceAll("\"", "\\\"");
		json = json.replaceAll("\"", "`");
		System.out.println(json);
		//json = "{`name`:`lisi`}";
        return json;  
	}

    public TransportStatus getTransportStatus() {
        return transportStatus;
    }

    public void setTransportStatus(TransportStatus transportStatus) {
        this.transportStatus = transportStatus;
    }

}
