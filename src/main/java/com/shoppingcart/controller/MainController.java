package com.shoppingcart.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.shoppingcart.dao.OrderDAO;
import com.shoppingcart.dao.ProductDAO;
import com.shoppingcart.entity.Product;
import com.shoppingcart.model.CartInfo;
import com.shoppingcart.model.CustomerInfo;
import com.shoppingcart.model.PaginationResult;
import com.shoppingcart.model.ProductInfo;
import com.shoppingcart.util.Utils;
import com.shoppingcart.validator.CustomerInfoValidator;

@Controller
public class MainController {
	@Autowired
	private ProductDAO productDAO;
	
	@Autowired
	private CustomerInfoValidator customerInfoValidator;
	
	@Autowired
	private OrderDAO orderDAO;
	
	@RequestMapping({ "/productList" })
	public String getAllProductInfos(Model model, @RequestParam(value = "name", defaultValue = "") String likeName,
			@RequestParam(value = "page", defaultValue = "1") int page) {
		final int maxResult = 5;
		final int maxNavigationPage = 10;
		PaginationResult<ProductInfo> productInfos = productDAO.getAllProductInfos(page, maxResult, maxNavigationPage,
				likeName);

		model.addAttribute("paginationProductInfos", productInfos);
		return "productList";
	}
	
	@RequestMapping(value = { "/productImage" }, method = RequestMethod.GET)
	public void productImage(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("code") String code) throws IOException {
		Product product = null;
		if (code != null) {
			product = productDAO.getProductByCode(code);
		}

		if (product != null && product.getImage() != null) {
			response.setContentType("image/jpeg, image/jpg, image/png, image/gif");
			response.getOutputStream().write(product.getImage());
		}
		response.getOutputStream().close();
	}

	@RequestMapping({ "/buyProduct" })
	public String buyProductHandler(HttpServletRequest request, Model model,
			@RequestParam(value = "code", defaultValue = "") String code) {
		Product product = null;

		if (code != null && code.length() > 0) {
			product = productDAO.getProductByCode(code);
		}
		if (product != null) {
			// Th??ng tin gi??? h??ng c?? th??? ???? l??u v??o trong Session tr?????c ????.
			CartInfo cartInfo = Utils.getCartInfoInSession(request);
			ProductInfo productInfo = new ProductInfo(product);//l???y code,name,price t??? product truy???n qua ProductInfo
			cartInfo.addProduct(productInfo, 1);
		}

		// Chuy???n sang trang danh s??ch c??c s???n ph???m ???? mua.
		return "redirect:/shoppingCart";
	}

	// GET: Hi???n th??? gi??? h??ng.
	@RequestMapping(value = { "/shoppingCart" }, method = RequestMethod.GET)
	public String shoppingCartHandler(HttpServletRequest request, Model model) {
		CartInfo cartInfo = Utils.getCartInfoInSession(request);

		model.addAttribute("cartForm", cartInfo);
		return "shoppingCart";
	}
	// POST: C???p nh???p s??? l?????ng cho c??c s???n ph???m ???? mua.
		@PostMapping(value = {"/shoppingCart"})
		public String shoppingCartUpdateQuantity(HttpServletRequest request, Model model,
				@ModelAttribute("cartForm") CartInfo cartForm) {
			CartInfo cartInfo = Utils.getCartInfoInSession(request);
			cartInfo.updateQuantity(cartForm);

			// Chuy???n sang trang danh s??ch c??c s???n ph???m ???? mua.
			return "redirect:/shoppingCart";
		}
		
		@GetMapping(value = {"/shoppingCartRemoveProduct"})
		public String removeProductHandler(HttpServletRequest request, Model model,
				@RequestParam(value = "code", defaultValue = "") String code) {
			Product product = null;

			if (code != null && code.length() > 0) {
				product = productDAO.getProductByCode(code);
			}

			if (product != null) {
				// Th??ng tin gi??? h??ng c?? th??? ???? l??u v??o trong Session tr?????c ????.
				CartInfo cartInfo = Utils.getCartInfoInSession(request);
				ProductInfo productInfo = new ProductInfo(product);
				cartInfo.removeProduct(productInfo);
			}

			// Chuy???n sang trang danh s??ch c??c s???n ph???m ???? mua.
			return "redirect:/shoppingCart";
		}


		// GET: Nh???p th??ng tin kh??ch h??ng.
		@GetMapping(value = { "/shoppingCartCustomer" })
		public String shoppingCartCustomerForm(HttpServletRequest request, Model model) {
			CartInfo cartInfo = Utils.getCartInfoInSession(request);

			// Ch??a mua m???t h??ng n??o.
			if (cartInfo.isEmpty()) {
				// Chuy???n t???i trang danh gi??? h??ng
				return "redirect:/shoppingCart";
			}

			CustomerInfo customerInfo = cartInfo.getCustomerInfo();
			if (customerInfo == null) {
				customerInfo = new CustomerInfo();
			}

			model.addAttribute("customerForm", customerInfo);
			return "shoppingCartCustomer";
		}
		
		// POST: Save th??ng tin kh??ch h??ng.
		@PostMapping(value = { "/shoppingCartCustomer" })
		public String shoppingCartCustomerSave(HttpServletRequest request, Model model,
				@ModelAttribute("customerForm") @Validated CustomerInfo customerForm, BindingResult result) {
			customerInfoValidator.validate(customerForm, result);
			// K???t qu??? Validate CustomerInfo.
			if (result.hasErrors()) {
				customerForm.setValid(false);
				return "shoppingCartCustomer";
			}

			customerForm.setValid(true);
			CartInfo cartInfo = Utils.getCartInfoInSession(request);
			cartInfo.setCustomerInfo(customerForm);
			// Chuy???n h?????ng sang trang x??c nh???n.
			return "redirect:/shoppingCartConfirmation";
		}

		// GET: Xem l???i th??ng tin ????? x??c nh???n.
		@GetMapping(value = {"/shoppingCartConfirmation"})
		public String shoppingCartConfirmationReview(HttpServletRequest request, Model model) {
			CartInfo cartInfo = Utils.getCartInfoInSession(request);

			// Ch??a mua m???t h??ng n??o.
			if (cartInfo.isEmpty()) {
				// Chuy???n t???i trang danh gi??? h??ng
				return "redirect:/shoppingCart";
			} else if (!cartInfo.isValidCustomer()) {
				// Chuy???n t???i trang nh???p th??ng tin kh??ch h??ng.
				return "redirect:/shoppingCartCustomer";
			}
			
			return "shoppingCartConfirmation";
		}
		// POST: G???i ????n h??ng (Save).
		@PostMapping(value = {"/shoppingCartConfirmation"})
		//@Transactional(propagation = Propagation.NEVER) // Tr??nh ngo???i l???: UnexpectedRollbackException
		public String shoppingCartConfirmationSave(HttpServletRequest request, Model model) {
			CartInfo cartInfo = Utils.getCartInfoInSession(request);

			// Ch??a mua m???t h??ng n??o.
			if (cartInfo.isEmpty()) {
				// Chuy???n t???i trang danh s??ch gi??? h??ng
				return "redirect:/shoppingCart";
			} else if (!cartInfo.isValidCustomer()) {
				// Chuy???n t???i trang nh???p th??ng tin kh??ch h??ng.
				return "redirect:/shoppingCartCustomer";
			}

			try {
				orderDAO.saveOrder(cartInfo);
			} catch (Exception e) {
				// C???n thi???t: Propagation.NEVER?
				return "shoppingCartConfirmation";
			}

			// X??a gi??? h??ng kh???i session.
			Utils.removeCartInfoInSession(request);

			// L??u th??ng tin ????n h??ng ???? x??c nh???n mua.
			Utils.storeLastOrderedCartInfoInSession(request, cartInfo);

			// Chuy???n h?????ng t???i trang ho??n th??nh mua h??ng.
			return "redirect:/shoppingCartFinalize";
		}
		
		@GetMapping(value = { "/shoppingCartFinalize" })
		public String shoppingCartFinalize(HttpServletRequest request, Model model) {
			CartInfo lastOrderedCart = Utils.getLastOrderedCartInfoInSession(request);

			if (lastOrderedCart == null) {
				return "redirect:/shoppingCart";
			}

			return "shoppingCartFinalize";
		}
	
}
