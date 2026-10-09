// Types miroirs des DTO du backend (com.stackclone.dto)

export interface UserSummary {
  id: number
  username: string
  reputation: number
  role: 'USER' | 'MODERATOR' | 'ADMIN'
}

export interface UserProfile extends UserSummary {
  bio: string | null
  createdAt: string
  questionCount: number
  answerCount: number
}

export interface AuthResponse {
  token: string
  user: UserSummary
}

export interface Page<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface QuestionSummary {
  id: number
  title: string
  excerpt: string
  score: number
  answerCount: number
  viewCount: number
  answered: boolean
  tags: string[]
  author: UserSummary
  createdAt: string
}

export interface Comment {
  id: number
  postId: number
  body: string
  author: UserSummary
  createdAt: string
}

export interface Answer {
  id: number
  questionId: number
  body: string
  score: number
  accepted: boolean
  author: UserSummary
  createdAt: string
  updatedAt: string | null
  myVote: number
  comments: Comment[]
}

export interface QuestionDetail {
  id: number
  title: string
  body: string
  score: number
  viewCount: number
  status: 'OPEN' | 'CLOSED' | 'DUPLICATE'
  tags: string[]
  author: UserSummary
  createdAt: string
  updatedAt: string | null
  myVote: number
  acceptedAnswerId: number | null
  comments: Comment[]
  answers: Answer[]
}

export interface TagUsage {
  id: number
  name: string
  description: string | null
  questionCount: number
}

export interface VoteResult {
  postId: number
  score: number
  myVote: number
}
