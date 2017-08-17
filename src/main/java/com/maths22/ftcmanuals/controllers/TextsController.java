package com.maths22.ftcmanuals.controllers;

import com.maths22.ftcmanuals.repositories.elasticsearch.DefinitionEsRepository;
import com.maths22.ftcmanuals.repositories.elasticsearch.ForumPostEsRepository;
import com.maths22.ftcmanuals.repositories.elasticsearch.RuleEsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TextsController {
    private final DefinitionEsRepository definitionEsRepository;

    private final ForumPostEsRepository forumPostEsRepository;

    private final RuleEsRepository ruleEsRepository;

    @Autowired
    public TextsController(DefinitionEsRepository definitionEsRepository, ForumPostEsRepository forumPostEsRepository, RuleEsRepository ruleEsRepository) {
        this.definitionEsRepository = definitionEsRepository;
        this.forumPostEsRepository = forumPostEsRepository;
        this.ruleEsRepository = ruleEsRepository;
    }

    @RequestMapping("/texts")
    public String list(Model model) {
        model.addAttribute("definitions", definitionEsRepository.findAll(Sort.by("category.keyword", "title.keyword")));
        model.addAttribute("rules", ruleEsRepository.findAll(Sort.by("number")));
        model.addAttribute("posts", forumPostEsRepository.findAll(Sort.by("forum", "category.keyword", "postNo")));
        return "texts/list";
    }

}
