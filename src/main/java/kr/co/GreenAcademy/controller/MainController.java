package kr.co.GreenAcademy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import kr.co.GreenAcademy.service.MainService;
import kr.co.GreenAcademy.vo.TestVO;

@Controller
public class MainController {

	@Autowired
	private MainService service;
	
	
	@GetMapping(value = {"/", "/index"})
	public String index(Model model) {
//		List<TestVO> list= service.selectTest();
//		model.addAttribute("list", list);
		return "member/login";
	}
}
