package com.example.vozdelas;

import com.google.firebase.Timestamp; // Import necessário para o tipo Timestamp

public class EducationalContent {
    private String id; // Para guardar o ID do documento do Firestore, se precisar
    private String category; // Ex: "Direitos Trabalhistas", "Mitos da Violência"
    private String title;
    private String subtitle;
    private String content; // O texto principal do conteúdo
    private String imageUrl; // Opcional: URL de uma imagem relacionada
    private String videoUrl; // Opcional: URL de um vídeo (ex: YouTube)
    private String externalLink; // Opcional: link para um artigo externo
    private Timestamp timestamp; // Para ordenar por data de publicação. Alterado para Timestamp

    public EducationalContent() {
        // Construtor vazio necessário para Firebase Firestore
    }

    public EducationalContent(String id, String category, String title, String subtitle, String content,
                              String imageUrl, String videoUrl, String externalLink, Timestamp timestamp) { // Alterado para Timestamp
        this.id = id;
        this.category = category;
        this.title = title;
        this.subtitle = subtitle;
        this.content = content;
        this.imageUrl = imageUrl;
        this.videoUrl = videoToEmbedUrl(videoUrl); // Chame o método auxiliar
        this.externalLink = externalLink;
        this.timestamp = timestamp; // Alterado para Timestamp
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoToEmbedUrl(videoUrl); // Chame o método auxiliar
    }

    public String getExternalLink() {
        return externalLink;
    }

    public void setExternalLink(String externalLink) {
        this.externalLink = externalLink;
    }

    public Timestamp getTimestamp() { // Alterado para Timestamp
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) { // Alterado para Timestamp
        this.timestamp = timestamp;
    }

    // Método auxiliar para converter URL de vídeo do YouTube em URL de embed
    // Isso será útil se você usar WebViews para exibir vídeos
    private String videoToEmbedUrl(String youtubeUrl) {
        if (youtubeUrl == null || youtubeUrl.isEmpty()) {
            return null;
        }
        // Exemplo: https://www.youtube.com/watch?v=dQw4w9WgXcQ
        // Torna-se: https://www.youtube.com/embed/dQw4w9WgXcQ
        String videoId = null;
        if (youtubeUrl.contains("youtu.be/")) {
            videoId = youtubeUrl.substring(youtubeUrl.lastIndexOf("/") + 1);
        } else if (youtubeUrl.contains("watch?v=")) {
            videoId = youtubeUrl.substring(youtubeUrl.lastIndexOf("v=") + 2);
            int ampersandIndex = videoId.indexOf('&');
            if (ampersandIndex != -1) {
                videoId = videoId.substring(0, ampersandIndex);
            }
        }

        if (videoId != null) {
            return "https://www.youtube.com/embed/" + videoId;
        }
        return youtubeUrl; // Retorna o original se não conseguir converter
    }
}