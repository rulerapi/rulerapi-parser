package com.maths22.ftcmanuals.controllers;

import com.maths22.ftcmanuals.exceptions.NotFoundException;
import com.maths22.ftcmanuals.models.GameManualSource;
import com.maths22.ftcmanuals.models.VBulletinForumSource;
import com.maths22.ftcmanuals.repositories.jpa.GameManualSourceRepository;
import com.maths22.ftcmanuals.repositories.jpa.VBulletinForumSourceRepository;
import com.maths22.ftcmanuals.services.GameManualImporter;
import com.maths22.ftcmanuals.services.VBulletinForumImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class SourceController {
    private final GameManualSourceRepository gameManualSourceRepository;

    private final VBulletinForumSourceRepository vBulletinForumSourceRepository;

    private final VBulletinForumImporter vBulletinForumImporter;

    private final GameManualImporter gameManualImporter;

    @Autowired
    public SourceController(GameManualSourceRepository gameManualSourceRepository, VBulletinForumSourceRepository vBulletinForumSourceRepository, VBulletinForumImporter vBulletinForumImporter, GameManualImporter gameManualImporter) {
        this.gameManualSourceRepository = gameManualSourceRepository;
        this.vBulletinForumSourceRepository = vBulletinForumSourceRepository;
        this.vBulletinForumImporter = vBulletinForumImporter;
        this.gameManualImporter = gameManualImporter;
    }

    @RequestMapping("/sources")
    public String list(Model model) {
        model.addAttribute("gameManuals", gameManualSourceRepository.findAll());
        model.addAttribute("ftcForumThreads", vBulletinForumSourceRepository.findAll());
        return "sources/list";
    }

    //TODO make this a post request
    @RequestMapping("/sources/importVBulletin/{id}")
    public String importVBulletin(@PathVariable long id, HttpServletRequest request) {
        VBulletinForumSource src = vBulletinForumSourceRepository.findById(id).orElseThrow(NotFoundException::new);
        src.setLastRetrieved(LocalDateTime.now());

        if (vBulletinForumImporter.importForumThread(src)) {
            vBulletinForumSourceRepository.save(src);
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + referer;
    }

    //TODO make this a post request
    @RequestMapping("/sources/importGameManual/{id}")
    public String importGameManual(@PathVariable long id, HttpServletRequest request) {
        GameManualSource src = gameManualSourceRepository.findById(id).orElseThrow(NotFoundException::new);
        src.setLastRetrieved(LocalDateTime.now());

        if (gameManualImporter.importGameManual(src)) {
            gameManualSourceRepository.save(src);
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + referer;
    }

    @RequestMapping("/sources/ingestAll")
    public String importGameManual(HttpServletRequest request) {
        List<GameManualSource> gmSrcs = gameManualSourceRepository.findAll();
        gmSrcs.forEach((src) -> {
            src.setLastRetrieved(LocalDateTime.now());

            if (gameManualImporter.importGameManual(src)) {
                gameManualSourceRepository.save(src);
            }
        });

        List<VBulletinForumSource> vbSrcs = vBulletinForumSourceRepository.findAll();
        vbSrcs.forEach((src) -> {
            src.setLastRetrieved(LocalDateTime.now());

            if (vBulletinForumImporter.importForumThread(src)) {
                vBulletinForumSourceRepository.save(src);
            }
        });


        String referer = request.getHeader("Referer");
        return "redirect:" + referer;
    }

}
