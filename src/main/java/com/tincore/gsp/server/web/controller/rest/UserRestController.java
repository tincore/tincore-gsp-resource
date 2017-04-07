package com.tincore.gsp.server.web.controller.rest;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({ "/user", "/me" })
public class UserRestController implements GpsServerRestController {

	@GetMapping
	public Map<String, String> user(Principal principal) {
		Map<String, String> map = new LinkedHashMap<>();
		if (principal != null){
			map.put("name", principal.getName());
		}
		return map;
	}

//	@GetMapping
//	public Principal user(Principal principal) {
//		return principal;
//	}


}