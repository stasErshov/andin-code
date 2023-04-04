package api

data class Attachment(
        val url: String,
        val description: String,
        val type: AttachmentType,
)

data class Author(
        val id: Long,
        val name: String,
        val avatar: String,
)

data class Comment(
        val id: Long,
        val postId: Long,
        val authorId: Long,
        val content: String,
        val published: Long,
        val likedByMe: Boolean,
        val likes: Int = 0,
)

data class CommentAuthor(
        val author: Author,
        val comment: Comment
)

data class Post(
        val id: Long,
        val authorId: Long,
        val content: String,
        val published: Long,
        val likedByMe: Boolean,
        val likes: Int = 0,
        var attachment: Attachment? = null,
)

data class FullPost(
        val author: Author,
        val post: Post,
        val comments: List<CommentAuthor>,
)
enum class AttachmentType {
    VIDEO, IMAGE, STRING
}