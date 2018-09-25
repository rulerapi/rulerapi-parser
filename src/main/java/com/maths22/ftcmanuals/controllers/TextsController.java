package com.maths22.ftcmanuals.controllers;

import com.maths22.ftcmanuals.repositories.elasticsearch.DefinitionEsRepository;
import com.maths22.ftcmanuals.repositories.elasticsearch.ForumPostEsRepository;
import com.maths22.ftcmanuals.repositories.elasticsearch.RuleEsRepository;
import com.maths22.ftcmanuals.repositories.elasticsearch.TextRepository;
import com.maths22.ftcmanuals.resources.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class TextsController {
    private final DefinitionEsRepository definitionEsRepository;

    private final ForumPostEsRepository forumPostEsRepository;

    private final RuleEsRepository ruleEsRepository;

    private final TextRepository textRepository;

    @Autowired
    public TextsController(DefinitionEsRepository definitionEsRepository, ForumPostEsRepository forumPostEsRepository, RuleEsRepository ruleEsRepository, TextRepository textRepository) {
        this.definitionEsRepository = definitionEsRepository;
        this.forumPostEsRepository = forumPostEsRepository;
        this.ruleEsRepository = ruleEsRepository;
        this.textRepository = textRepository;
    }

    @RequestMapping("/texts")
    public String list(Model model) {
        model.addAttribute("definitions", definitionEsRepository.findAll(Sort.by("category.keyword", "title.keyword")));
        model.addAttribute("rules", ruleEsRepository.findAll(Sort.by("number.keyword")));
        model.addAttribute("posts", forumPostEsRepository.findAll(Sort.by("forum", "category.keyword", "postNo")));
        return "texts/list";
    }

    @CrossOrigin
    @RequestMapping(value = "/texts/search", produces = "application/json")
    @ResponseBody
    public Page<?> list(@PageableDefault Pageable pageable, @RequestBody String query) {
        return textRepository.search(query, pageable);
    }
}
