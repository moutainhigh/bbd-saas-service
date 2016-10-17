package com.bbt.demo.provider;

import com.bbd.saas.api.mongo.OrderService;
import com.bbd.saas.vo.OrderQueryVO;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class OrderServiceTest {
	@Autowired
	private OrderService orderService;

	private ObjectId uId = new ObjectId("573c5f421e06c8275c08183c");
	//junit.framework.TestCase时用
	public void setUp() throws Exception{
        System.out.println("set up");
        // 生成成员变量的实例
    }
	//junit.framework.TestCase时用
    public void tearDown() throws Exception{
        System.out.println("tear down");
    }


	@Test
	public void testElemMatch() throws Exception{
		//设置查询条件
		OrderQueryVO tradeQueryVO = new OrderQueryVO();
		/*tradeQueryVO.uId = uId;
		tradeQueryVO.tradeStatus = -1;
		tradeQueryVO.noLike = null;
		tradeQueryVO.rcvKeyword = "奎文";
		tradeQueryVO.dateAddStart = "2016-06-21";
		tradeQueryVO.dateAddEnd = "2016-06-21";
		//若此方法超时，则把设置快件数量、揽件员、订单状态提到controller中
		PageModel<Order> tradePage = orderService.findOrders();*/
		Assert.isTrue(true);//无用
	}



}
