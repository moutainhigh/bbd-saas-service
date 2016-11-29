package com.bbt.demo.provider.mysql;

import com.bbd.saas.api.mysql.ExpressCompanyService;
import com.bbd.saas.api.mysql.SiteMySqlService;
import com.bbd.saas.models.ExpressCompany;
import com.bbd.saas.models.SiteMySql;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class SiteMysqlTest {
	@Autowired
	private SiteMySqlService siteMySqlService;
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
	public void testFind() throws Exception{
		try {
			SiteMySql siteMySql = this.siteMySqlService.selectIdBySiteId("579aed03c35cc55525031ee9");
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.isTrue(true);//无用
	}
	@Test
	public void testStringBuffer() throws Exception{
		StringBuffer msg = new StringBuffer();
		System.out.println("00000"+msg.toString()+"====");
	}

}
