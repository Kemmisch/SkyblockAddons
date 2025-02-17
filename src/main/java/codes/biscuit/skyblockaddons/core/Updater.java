package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.SkyblockAddonsASMTransformer;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.data.skyblockdata.OnlineData;
import lombok.Getter;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static codes.biscuit.skyblockaddons.core.Translations.getMessage;
import static net.minecraftforge.common.ForgeVersion.Status.*;

/**
 * This class is the SkyblockAddons updater. It checks for updates by reading version information from {@link OnlineData.UpdateInfo}.
 */
public class Updater {

    private static final Pattern VERSION_PATTERN = Pattern.compile("(?<major>[0-9])\\.(?<minor>[0-9])\\.(?<patch>[0-9]).*");

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Logger logger = SkyblockAddons.getLogger();

    private ComparableVersion target = null;

    @Getter
    private String messageToRender;
    private String downloadLink;
    private String changelogLink;
    private String note;

    private boolean hasUpdate = false;
    private boolean isPatch = false;
    private boolean sentUpdateMessage = false;

    /**
     * Returns whether the update notification message has already been sent.
     *
     * @return {@code true} if the update notification message has already been sent, {@code false} otherwise
     */
    public boolean hasSentUpdateMessage() {
        return sentUpdateMessage;
    }

    /**
     * Returns whether there is an update available
     *
     * @return {@code true} if there is an update available, {@code false} otherwise.
     */
    public boolean hasUpdate() {
        return hasUpdate;
    }

    /**
     * Checks the online data for an update and sets the correct message to be displayed.
     */
    public void checkForUpdate() {
        logger.info("Checking to see if an update is available...");
        OnlineData.UpdateInfo updateInfo = main.getOnlineData().getUpdateInfo();

        // Variables reset for testing update checker notifications
        sentUpdateMessage = false;
        main.getRenderListener().setUpdateMessageDisplayed(false);

        if (updateInfo == null) {
            logger.error("Update check failed: Update info is null!");
            return;
        }

        ComparableVersion latestRelease = null;
        ComparableVersion latestBeta = null;
        ComparableVersion current = new ComparableVersion(SkyblockAddons.VERSION);
        boolean isCurrentBeta = isBetaVersion(current);
        boolean latestReleaseExists = updateInfo.getLatestRelease() != null && !updateInfo.getLatestRelease().isEmpty();
        boolean latestBetaExists = updateInfo.getLatestBeta() != null && !updateInfo.getLatestBeta().isEmpty();
        int releaseDiff = 0;
        int betaDiff = 0;

        if (latestReleaseExists) {
            latestRelease = new ComparableVersion(updateInfo.getLatestRelease());
            releaseDiff = latestRelease.compareTo(current);
        } else {
            if (!isCurrentBeta) {
                logger.error("Update check failed: Current version is a release version and key `latestRelease` is null " +
                        "or empty.");
                return;
            } else {
                logger.warn("Key `latestRelease` is null or empty, skipping!");
            }
        }

        if (isCurrentBeta) {
            if (latestBetaExists) {
                latestBeta = new ComparableVersion(updateInfo.getLatestBeta());
                betaDiff = latestBeta.compareTo(current);
            } else {
                if (latestRelease == null) {
                    logger.error("Update check failed: Keys `latestRelease` and `latestBeta` are null or empty.");
                    return;
                } else {
                    logger.warn("Key `latestBeta` is null or empty, skipping!");
                }
            }
        }

        ForgeVersion.Status status = null;
        if (!isCurrentBeta) {
            if (releaseDiff == 0) {
                status = UP_TO_DATE;
            } else if (releaseDiff < 0) {
                status = AHEAD;
            } else {
                status = OUTDATED;
                target = latestRelease;
            }
        } else {
            String currentVersionString = current.toString();

            // If release is newer than this beta, target release
            if (latestReleaseExists) {
                ComparableVersion currentWithoutPrerelease = new ComparableVersion(currentVersionString.substring(0,
                        currentVersionString.indexOf('-')));

                if (releaseDiff > 0 || latestRelease.compareTo(currentWithoutPrerelease) == 0) {
                    status = OUTDATED;
                    target = latestRelease;
                } else if (!latestBetaExists && releaseDiff < 0) {
                    status = AHEAD;
                } else if (releaseDiff == 0) {
                    logger.warn("The current beta version ({}) matches the latest release version. " +
                            "There is probably something wrong with the online data.", currentVersionString);
                    status = UP_TO_DATE;
                }
            }

            if (status == null) {
                if (betaDiff == 0) {
                    status = UP_TO_DATE;
                } else if (betaDiff < 0) {
                    status = AHEAD;
                } else {
                    status = BETA_OUTDATED;
                    target = latestBeta;
                }
            }
        }

        if (status == OUTDATED || status == BETA_OUTDATED) {
            hasUpdate = true;

            String currentVersion = current.toString();
            String targetVersion = target.toString();

            logger.info("Found an update: {}", targetVersion);

            if (status == OUTDATED) {
                targetVersion = updateInfo.getLatestRelease();
                downloadLink = updateInfo.getReleaseDownload();
                changelogLink = updateInfo.getReleaseChangelog();
                note = updateInfo.getReleaseNote();
            } else {
                targetVersion = updateInfo.getLatestBeta();
                downloadLink = updateInfo.getBetaDownload();
                changelogLink = updateInfo.getBetaChangelog();
                note = updateInfo.getBetaNote();
            }

            try {
                Matcher currentMatcher = VERSION_PATTERN.matcher(currentVersion);
                Matcher targetMatcher = VERSION_PATTERN.matcher(targetVersion);

                // It's a patch if the major & minor numbers are the same & the player isn't upgrading from a beta.
                isPatch = currentMatcher.matches() && targetMatcher.matches() &&
                        currentMatcher.group("major").equals(targetMatcher.group("major")) &&
                        currentMatcher.group("minor").equals(targetMatcher.group("minor")) &&
                        !isCurrentBeta;
            } catch (Exception ex) {
                logger.warn("Couldn't parse update version numbers... This shouldn't affect too much.", ex);
            }

            if (isPatch) {
                messageToRender = getMessage("messages.updateChecker.notificationBox.patchAvailable", targetVersion);
            } else if(status == BETA_OUTDATED) {
                messageToRender = getMessage("messages.updateChecker.notificationBox.betaAvailable", targetVersion);
            } else {
                messageToRender = getMessage("messages.updateChecker.notificationBox.majorAvailable", targetVersion);
            }
        } else if (status == AHEAD) {
            if (!SkyblockAddonsASMTransformer.isDeobfuscated()) {
                logger.warn("The current version is newer than the latest version. Please tell an SBA developer to update" +
                        " the online data.");
            } else {
                logger.error("The current version is newer than the latest version. You're doing something wrong.");
                logger.error("Current: {}", current);
                logger.error("Latest: {}", latestRelease);
                logger.error("Latest Beta: {}", latestBeta);
                logger.error("Release Diff: {}", releaseDiff);
                logger.error("Beta Diff: {}", betaDiff);
            }
        } else {
            logger.info("Up to date!");
        }
    }

    public void sendUpdateMessage() {
        if (sentUpdateMessage) {
            return;
        }

        String targetVersion = target.toString();

        main.getUtils().sendMessage("§7§m----------------§7[ §b§lSkyblockAddons §7]§7§m----------------", false);

        ChatComponentText newUpdate = new ChatComponentText(
                String.format("§b%s\n", getMessage("messages.updateChecker.newUpdateAvailable", targetVersion))
        );

        if (note != null && !note.isEmpty()) {
            ChatComponentText versionNote = new ChatComponentText("\n" + ColorCode.RED + note + "\n");
            newUpdate.appendSibling(versionNote);
        }

        /*
        ChatComponentText viewChangelog = new ChatComponentText(
                String.format("§b%s\n", getMessage("messages.updateChecker.wantToViewPatchNotes", targetVersion)));
        ChatComponentText joinDiscord = new ChatComponentText(
                String.format("§b%s\n", getMessage("messages.updateChecker.joinDiscord", targetVersion))
        );
        newUpdate.appendSibling(viewChangelog).appendSibling(joinDiscord);
        */
        main.getUtils().sendMessage(newUpdate, false);

        ChatComponentText downloadButton;
        ChatComponentText openModsFolderButton;
        ChatComponentText changelogButton;

        downloadButton = new ChatComponentText(
                String.format("§b§l[%s]", getMessage("messages.updateChecker.downloadButton", targetVersion))
        );

        if (downloadLink != null && !downloadLink.isEmpty()) {
            downloadButton.setChatStyle(
                    downloadButton.getChatStyle().setChatClickEvent(
                            new ClickEvent(ClickEvent.Action.OPEN_URL, downloadLink)
                    ).setChatHoverEvent(
                            new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    new ChatComponentText("§7" + getMessage("messages.clickToOpenLink"))
                            )
                    )
            );
        } else {
            downloadButton.setChatStyle(
                    downloadButton.getChatStyle().setStrikethrough(true).setChatHoverEvent(
                            new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    new ChatComponentText("§7" + getMessage("messages.updateChecker.noDownloadAvailable"))
                            )
                    )
            );
        }
        downloadButton.appendText(" ");

        openModsFolderButton = new ChatComponentText(
                String.format("§e§l[%s]", getMessage("messages.updateChecker.openModFolderButton"))
        );
        openModsFolderButton.setChatStyle(
                openModsFolderButton.getChatStyle().setChatClickEvent(
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sba folder")
                ).setChatHoverEvent(
                        new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new ChatComponentText("§7" + getMessage("messages.clickToOpenFolder"))
                        )
                )
        );
        downloadButton.appendSibling(openModsFolderButton);

        if (changelogLink != null && !changelogLink.isEmpty()) {
            changelogButton = new ChatComponentText(
                    String.format(" §9§l[%s]", getMessage("messages.updateChecker.changelogButton"))
            );
            changelogButton.setChatStyle(
                    changelogButton.getChatStyle().setChatClickEvent(
                            new ClickEvent(ClickEvent.Action.OPEN_URL, changelogLink)
                    ).setChatHoverEvent(
                            new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    new ChatComponentText("§7" + getMessage("messages.clickToOpenLink"))
                            )
                    )
            );
            downloadButton.appendSibling(changelogButton);
        }

        main.getUtils().sendMessage(downloadButton, false);
        main.getUtils().sendMessage("§7§m--------------------------------------------------", false);

        sentUpdateMessage = true;
    }

    /**
     * Returns whether the given version is a beta version
     *
     * @param version the version to check
     * @return {@code true} if the given version is a beta version, {@code false} otherwise
     */
    private boolean isBetaVersion(ComparableVersion version) {
        return version.toString().contains("b");
    }
}
