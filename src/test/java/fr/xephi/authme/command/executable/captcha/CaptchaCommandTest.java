package fr.xephi.authme.command.executable.captcha;

import fr.xephi.authme.data.LoginCaptchaManager;
import fr.xephi.authme.data.RegistrationCaptchaManager;
import fr.xephi.authme.data.auth.PlayerCache;
import fr.xephi.authme.data.limbo.LimboService;
import fr.xephi.authme.message.MessageKey;
import fr.xephi.authme.service.CommonService;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Test for {@link CaptchaCommand}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CaptchaCommandTest {

    @InjectMocks
    private CaptchaCommand command;

    @Mock
    private LoginCaptchaManager loginCaptchaManager;

    @Mock
    private RegistrationCaptchaManager registrationCaptchaManager;

    @Mock
    private PlayerCache playerCache;

    @Mock
    private CommonService commandService;

    @Mock
    private LimboService limboService;

    @Test
    public void shouldDetectIfPlayerIsLoggedIn() {
        // given
        String name = "creeper011";
        Player player = mockPlayerWithName(name);
        given(playerCache.isAuthenticated(name)).willReturn(true);

        // when
        command.executeCommand(player, Collections.singletonList("123"));

        // then
        verify(commandService).send(player, MessageKey.ALREADY_LOGGED_IN_ERROR);
    }

    @Test
    public void shouldShowLoginUsageIfCaptchaIsNotRequired() {
        // given
        String name = "bobby";
        Player player = mockPlayerWithName(name);
        given(playerCache.isAuthenticated(name)).willReturn(false);
        given(loginCaptchaManager.isCaptchaRequired(name)).willReturn(false);

        // when
        command.executeCommand(player, Collections.singletonList("1234"));

        // then
        verify(commandService).send(player, MessageKey.USAGE_LOGIN);
        verify(loginCaptchaManager).isCaptchaRequired(name);
        verifyNoMoreInteractions(loginCaptchaManager);
    }

    @Test
    public void shouldHandleCorrectCaptchaInput() {
        // given
        String name = "smith";
        Player player = mockPlayerWithName(name);
        given(playerCache.isAuthenticated(name)).willReturn(false);
        given(loginCaptchaManager.isCaptchaRequired(name)).willReturn(true);
        String captchaCode = "3991";
        given(loginCaptchaManager.checkCode(name, captchaCode)).willReturn(true);

        // when
        command.executeCommand(player, Collections.singletonList(captchaCode));

        // then
        verify(loginCaptchaManager).isCaptchaRequired(name);
        verify(loginCaptchaManager).checkCode(name, captchaCode);
        verifyNoMoreInteractions(loginCaptchaManager);
        verify(commandService).send(player, MessageKey.CAPTCHA_SUCCESS);
        verify(commandService).send(player, MessageKey.LOGIN_MESSAGE);
        verify(limboService).unmuteMessageTask(player);
        verifyNoMoreInteractions(commandService);
    }

    @Test
    public void shouldHandleWrongCaptchaInput() {
        // given
        String name = "smith";
        Player player = mockPlayerWithName(name);
        given(playerCache.isAuthenticated(name)).willReturn(false);
        given(loginCaptchaManager.isCaptchaRequired(name)).willReturn(true);
        String captchaCode = "2468";
        given(loginCaptchaManager.checkCode(name, captchaCode)).willReturn(false);
        String newCode = "1337";
        given(loginCaptchaManager.generateCode(name)).willReturn(newCode);

        // when
        command.executeCommand(player, Collections.singletonList(captchaCode));

        // then
        verify(loginCaptchaManager).isCaptchaRequired(name);
        verify(loginCaptchaManager).checkCode(name, captchaCode);
        verify(loginCaptchaManager).generateCode(name);
        verifyNoMoreInteractions(loginCaptchaManager);
        verify(commandService).send(player, MessageKey.CAPTCHA_WRONG_ERROR, newCode);
        verifyNoMoreInteractions(commandService);
    }

    private static Player mockPlayerWithName(String name) {
        Player player = mock(Player.class);
        given(player.getName()).willReturn(name);
        return player;
    }
}
