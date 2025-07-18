package com.synthetic.byshop_prototype;

import com.synthetic.human_core_starter.domain.command.Command;
import com.synthetic.human_core_starter.domain.command.CommandDispatcher;
import com.synthetic.human_core_starter.infrastructure.WeylandWatchingYou;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    private final CommandDispatcher commandDispatcher;

    public Controller(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }
    @WeylandWatchingYou
    @PostMapping
    public ResponseEntity<Void> executeCommand(@Valid @RequestBody Command command) {
        commandDispatcher.dispatch(command);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
