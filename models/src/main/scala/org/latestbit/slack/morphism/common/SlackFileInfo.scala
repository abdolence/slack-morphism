package org.latestbit.slack.morphism.common

import cats.data._

case class SlackFileId( value: String ) extends AnyVal

case class SlackFileMimeType( value: String ) extends AnyVal

case class SlackPrettyFileType( value: String ) extends AnyVal

case class SlackFileUploadMode( value: String ) extends AnyVal

case class SlackFileInfo(
    id: SlackFileId,
    created: SlackDateTime,
    timestamp: SlackDateTime,
    name: String,
    user: SlackUserId,
    username: Option[String] = None,
    typeInfo: SlackFileTypeInfo = SlackFileTypeInfo(),
    size: Long,
    title: Option[String] = None,
    mode: Option[SlackFileUploadMode] = None,
    flags: SlackFileFlags = SlackFileFlags(),
    url_private: Option[String] = None,
    url_private_download: Option[String] = None,
    resolutionInfo: SlackFileResolutionInfo = SlackFileResolutionInfo(),
    thumbnails: SlackFileThumbnails = SlackFileThumbnails(),
    permalink: Option[String] = None,
    permalink_public: Option[String] = None,
    comments_count: Option[Long] = None,
    channels: Option[NonEmptyList[SlackChannelId]] = None,
    preview: Option[String] = None,
    preview_highlight: Option[String] = None,
)


case class SlackFileFlags(
    editable: Option[Boolean] = None,
    is_external: Option[Boolean] = None,
    is_public: Option[Boolean] = None,
    public_url_shared: Option[Boolean] = None,
    display_as_bot: Option[Boolean] = None,
    is_starred: Option[Boolean] = None,
    has_rich_preview: Option[Boolean] = None,
)

case class SlackFileTypeInfo(
    mimetype: Option[SlackFileMimeType] = None,
    filetype: Option[SlackFileType] = None,
    pretty_type: Option[SlackPrettyFileType] = None,
    external_type: Option[String] = None,
)

case class SlackFileResolutionInfo(
    original_w: Option[Long] = None,
    original_h: Option[Long] = None,
    image_exif_rotation: Option[Long] = None
)
