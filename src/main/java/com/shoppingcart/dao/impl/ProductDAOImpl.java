package com.shoppingcart.dao.impl;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.shoppingcart.dao.ProductDAO;
import com.shoppingcart.entity.Product;
import com.shoppingcart.model.PaginationResult;
import com.shoppingcart.model.ProductInfo;

@Repository
@Transactional
public class ProductDAOImpl implements ProductDAO{
	@Autowired
	private SessionFactory sessionFactory;

	//Entity Product chứa các thông tin code, name, price, image, createDate -->các thông tin này sẽ có dữ liệu tương ứng trong database
	//Model ProductInfo chứa các thông tin code, name, price, fileData -->khác với Entity Product ở thuộc tính fileData
	//Vì khi thực hiện tính năng upload hình ảnh trong lúc tạo Product mới, ta phải để kiểu của nó là CommonsMultipartFile chứ ko phải kiểu byte[]
	//kiểu byte[] chỉ dùng để lưu hình ảnh này dưới db
	public PaginationResult<ProductInfo> getAllProductInfos(int page, int maxResult, int maxNavigationPage,
			String likeName) {
		Session session = sessionFactory.getCurrentSession();
		//SELECT PRO.code, PRO.name, PRO.price FROM Product PRO -->cách thường sử dụng
		//muốn sử dụng SELECT NEW " + ProductInfo.class.getName() thì trong class ProductInfo phải khai báo Constructor
		//có 3 tham số là code(String), name(String), price(double)
		String hql = "SELECT NEW " + ProductInfo.class.getName() + " (PRO.code, PRO.name, PRO.price) FROM Product PRO";
		if (likeName != null && likeName.length() > 0) {
			hql += " WHERE LOWER(PRO.code) LIKE :LIKENAME ";
		}
		hql += " ORDER BY PRO.createDate DESC ";
		
		Query<ProductInfo> query = session.createQuery(hql);
		if (likeName != null && likeName.length() > 0) {
			query.setParameter("LIKENAME", "%" + likeName.toLowerCase() + "%");
		}
		List<ProductInfo> productInfos = query.list();
		return new PaginationResult<ProductInfo>(query, page, maxResult);
	}

	public Product getProductByCode(String code) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "SELECT PRO FROM Product PRO WHERE PRO.code = :CODE";
		Query<Product> query = session.createQuery(hql);
		query.setParameter("CODE", code);
		Product product = (Product) query.uniqueResult();
		return product;
	}

	@Override
	public ProductInfo getProductInfoByCode(String code) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveProductInfo(ProductInfo productInfo) {
		// TODO Auto-generated method stub
		
	}
}
