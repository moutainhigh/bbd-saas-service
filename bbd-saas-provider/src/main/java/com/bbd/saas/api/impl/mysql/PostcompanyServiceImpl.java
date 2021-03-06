package com.bbd.saas.api.impl.mysql;

import com.bbd.saas.api.mysql.PostcompanyService;
import com.bbd.saas.dao.mysql.PostcompanyDao;
import com.bbd.saas.models.Postcompany;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 公司Service实现
 * Created by luobotao on 2016/4/16.
 */
@Service("postcompanyService")
@Transactional
public class PostcompanyServiceImpl implements PostcompanyService {
	@Resource
	private PostcompanyDao postcompanyDao;

	public PostcompanyDao getPostcompanyDao() {
		return postcompanyDao;
	}

	public void setPostcompanyDao(PostcompanyDao postcompanyDao) {
		this.postcompanyDao = postcompanyDao;
	}

	/**
	 * 根据公司状态获取该状态下的所有公司列表
	 * @return
     */
	@Override
	@Transactional(readOnly = true)
	public List<Postcompany> selectAllByStatus(String sta) {
		return postcompanyDao.selectAllByStatus(sta);
	}
	/**
	 * 获取所有公司列表
	 * @return
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Postcompany> selectAll() {
		return postcompanyDao.selectAll();
	}
	/**
	 * 根据公司ID获取公司信息
	 * @param id
	 * @return
     */
	@Override
	@Transactional(readOnly = true)
	public Postcompany selectPostmancompanyById(Integer id){
		return postcompanyDao.selectPostmancompanyById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public Postcompany findOneByCode(String companycode) {
		return this.postcompanyDao.selectOneByCode(companycode);
	}

	/**
	 * 插入一条新公司
	 * @param postcompany
	 * @return
	 */
	@Override
	@Transactional(readOnly = true)
	public Postcompany insertCompany(Postcompany postcompany){
		postcompanyDao.insertCompany(postcompany);
		return postcompany;
	}

	/**
	 * 更新公司
	 * @param postcompany
     */
	@Override
	public Postcompany updateCompany(Postcompany postcompany) {
		postcompanyDao.updateCompany(postcompany);
		return postcompany;
	}
}
